package de.atennert.lcarswm.window

import de.atennert.lcarswm.drawing.UIDrawing
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.events.ReparentEvent
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.system.wrapXConfigureWindow
import de.atennert.lcarswm.system.wrapXSendEvent
import de.atennert.rx.*
import de.atennert.rx.operators.*
import de.atennert.rx.util.Tuple
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
sealed class WindowToMonitorEvent(val window: ManagedWmWindow<Window>, val monitor: Monitor<RROutput>?)
@ExperimentalForeignApi
class WindowToMonitorSetEvent(window: ManagedWmWindow<Window>, monitor: Monitor<RROutput>) : WindowToMonitorEvent(window, monitor)
@ExperimentalForeignApi
class WindowToMonitorRemoveEvent(window: ManagedWmWindow<Window>) : WindowToMonitorEvent(window, null)

/**
 *
 */
@ExperimentalForeignApi
class PosixWindowCoordinator(
    private val logger: Logger,
    eventStore: EventStore,
    monitorManager: MonitorManager<RROutput>,
    windowFactory: WindowFactory<Window>,
    windowList: WindowList,
    rootWindowDrawer: UIDrawing,
    private val display: CPointer<Display>?,
) : WindowCoordinator {
    private val windowToMonitorEventSj = Subject<WindowToMonitorEvent>()
    val windowToMonitorEventObs = windowToMonitorEventSj.asObservable()

    private val windowsOnMonitorsSj = BehaviorSubject(emptyMap<ManagedWmWindow<Window>, Monitor<*>>())
    val windowsOnMonitorsObs = windowsOnMonitorsSj.asObservable()
    private var windowsOnMonitors by windowsOnMonitorsSj

    private val moveWindowToNextMonitorSj = Subject<Window>()
    private val moveWindowToNextMonitorObs = moveWindowToNextMonitorSj.asObservable()

    private val moveWindowToPrevMonitorSj = Subject<Window>()
    private val moveWindowToPrevMonitorObs = moveWindowToPrevMonitorSj.asObservable()

    private val filterForBadParenting: Operator<ReparentEvent, Window> = Operator { source ->
        source.apply(withLatestFrom(windowList.windowsObs))
            .apply(filter { (event, windows) ->
                val window = windows.find { it.id == event.id }
                window != null && window.frame != event.parentId
            })
            .apply(map { (event, _) -> event.id })
    }

    val rearrangeObs = monitorManager.monitorsObs
        .apply(filter { it.isNotEmpty() })
        .apply(map { updatedMonitors ->
            val primaryMonitor = updatedMonitors.find { it.isPrimary } ?: updatedMonitors[0]

            val updatedWindows = windowsOnMonitors
                .map { (window, monitor) ->
                    Pair(window, updatedMonitors.getOrElse(updatedMonitors.indexOf(monitor)) { primaryMonitor })
                }
            updatedWindows
        })

    val combinedMeasurementsObs = windowsOnMonitorsObs
        .apply(map { updatedWindows ->
            updatedWindows.map {
                Observable.of(Tuple(it.value.windowMeasurements, it.value.screenMode, it.key))
            }
        })

    val measurementsObs = combinedMeasurementsObs
        .apply(switchMap { updatedWindows -> Observable.merge(*updatedWindows.toTypedArray()) })

    val titleObs = eventStore.propertyNotifyNameObs
        .apply(withLatestFrom(windowList.windowsObs))
        .apply(map { (windowId, windows) -> windows.find { it.id == windowId } })
        .apply(filterNotNull())

    init {
        val subscription = Subscription()
            .closeWith { this.unsubscribe() }

        subscription.add(
            windowToMonitorEventObs
                .subscribe(NextObserver {
                    if (it is WindowToMonitorSetEvent) {
                        windowsOnMonitors += it.window to it.monitor!!
                    } else {
                        windowsOnMonitors -= it.window
                    }
                })
        )

        // create and open windows
        subscription.add(
            eventStore.mapObs
                .apply(filter { !windowList.isManaged(it) })
                .apply(bufferWhile(
                    monitorManager.primaryMonitorObs.apply(filter { it == null }),
                    monitorManager.primaryMonitorObs.apply(filterNotNull())
                ))
                .apply(filter { it.isNotEmpty() })
                .apply(
                    withLatestFrom(
                        monitorManager.primaryMonitorObs
                            .apply(filterNotNull()),
                    )
                )
                .subscribe(NextObserver { (windowIds, monitor) ->
                    for (windowId in windowIds) {
                        logger.logDebug("PosixWindowCoordinator::init::create window $windowId")
                        windowFactory.createWindow(windowId)
                            ?.let {
                                if (it is ManagedWmWindow) {
                                    logger.logDebug("PosixWindowCoordinator::init::open window $windowId")
                                    windowToMonitorEventSj.next(WindowToMonitorSetEvent(it, monitor))
                                    it.open(monitor.windowMeasurements, monitor.screenMode)
                                    windowList.add(it)
                                }
                            }
                    }
                })
        )

        // remove windows
        subscription.add(
            eventStore.destroyObs
                .apply(
                    mergeWith(
                        eventStore.unmapObs,
                        eventStore.reparentObs.apply(filterForBadParenting)
                    )
                )
                .apply(withLatestFrom(windowList.windowsObs))
                .subscribe(NextObserver { (windowId, windows) ->
                    windows.find { w -> w.id == windowId }
                        ?.let {
                            logger.logDebug("PosixWindowCoordinator::init::remove window $windowId")
                            windowList.remove(it)
                            windowToMonitorEventSj.next(WindowToMonitorRemoveEvent(it))
                            it.close()
                        }
                })
        )
        subscription.add(eventStore.unmapObs
            .subscribe(NextObserver { rootWindowDrawer.drawWindowManagerFrame() }))

        // rearrange windows
        subscription.add(rearrangeObs.subscribe(NextObserver { updatedWindows ->
            windowsOnMonitors = updatedWindows.toMap()
        }))

        subscription.add(measurementsObs
            .subscribe(NextObserver { (measurements, screenMode, window) ->
                window.moveResize(measurements, screenMode)
            })
        )
        subscription.add(combinedMeasurementsObs
            .subscribe(NextObserver { rootWindowDrawer.drawWindowManagerFrame() }))

        // configure window
        subscription.add(
            eventStore.configureRequestObs
                .apply(withLatestFrom(windowList.windowsObs))
                // TODO should filter using windowsOnMonitors
                .apply(filter { (configureRequest, windows) ->
                    windows.any { it.id == configureRequest.window && (it !is PosixTransientWindow || !it.isTransientForRoot) }
                })
                .apply(map { (configureRequest) -> configureRequest })
                .apply(switchMap { configureRequest ->
                    Observable.of(configureRequest)
                        .apply(withLatestFrom(Observable.of(windowsOnMonitors.firstNotNullOf { entry ->
                            if (entry.key.id == configureRequest.window) {
                                entry.value
                            } else {
                                null
                            }
                        })))
                })
                .subscribe(NextObserver { (configureRequest, monitor) ->
                    adjustWindowToScreen(configureRequest, monitor.windowMeasurements)
                })
        )

        subscription.add(
            eventStore.configureRequestObs
                .apply(withLatestFrom(windowList.windowsObs))
                // TODO should filter using windowsOnMonitors
                .apply(filter { (configureRequest, windows) ->
                    !windows.any { it.id == configureRequest.window && (it !is PosixTransientWindow || !it.isTransientForRoot) }
                })
                .apply(map { (configureRequest) -> configureRequest })
                .subscribe(NextObserver { configureRequest ->
                    forwardConfigureRequest(configureRequest)
                })
        )

        // move windows
        subscription.add(moveWindowToNextMonitorObs
            .apply(map { windowId -> windowsOnMonitors.keys.find { it.id == windowId } })
            .apply(filterNotNull())
            .apply(withLatestFrom(windowsOnMonitorsObs, monitorManager.monitorsObs))
            .apply(map { (window, windowsOnMonitors, monitors) ->
                val currentMonitorIndex = monitors.indexOf(windowsOnMonitors[window])
                Tuple(window, monitors[(currentMonitorIndex + 1).rem(monitors.size)])
            })
            .subscribe(NextObserver { (window, nextMonitor) ->
                windowToMonitorEventSj.next(WindowToMonitorSetEvent(window, nextMonitor))
                rootWindowDrawer.drawWindowManagerFrame()
            })
        )

        subscription.add(moveWindowToPrevMonitorObs
            .apply(map { windowId -> windowsOnMonitors.keys.find { it.id == windowId } })
            .apply(filterNotNull())
            .apply(withLatestFrom(windowsOnMonitorsObs, monitorManager.monitorsObs))
            .apply(map { (window, windowsOnMonitors, monitors) ->
                val currentMonitorIndex = monitors.indexOf(windowsOnMonitors[window])
                Tuple(
                    window,
                    monitors[if (currentMonitorIndex == 0) monitors.size - 1 else currentMonitorIndex - 1]
                )
            })
            .subscribe(NextObserver { (window, prevMonitor) ->
                windowToMonitorEventSj.next(WindowToMonitorSetEvent(window, prevMonitor))
                rootWindowDrawer.drawWindowManagerFrame()
            })
        )

        // edit window title
        subscription.add(titleObs
            .subscribe(NextObserver { window -> window.updateTitle()}))
    }

    override fun moveWindowToNextMonitor(windowId: Window) {
        moveWindowToNextMonitorSj.next(windowId)
    }

    override fun moveWindowToPreviousMonitor(windowId: Window) {
        moveWindowToPrevMonitorSj.next(windowId)
    }

    override fun moveWindowToMonitor(windowId: Window, monitor: Monitor<RROutput>) {
        val window = windowsOnMonitors.keys.single { it.id == windowId }
        windowToMonitorEventSj.next(WindowToMonitorSetEvent(window, monitor))
    }

    private fun adjustWindowToScreen(
        configureRequest: XConfigureRequestEvent,
        measurements: WindowMeasurements
    ) {
        val e = nativeHeap.alloc<XEvent>()
        e.type = ConfigureNotify
        e.xconfigure.event = configureRequest.window
        e.xconfigure.window = configureRequest.window
        e.xconfigure.x = measurements.x
        e.xconfigure.y = measurements.y
        e.xconfigure.width = measurements.width
        e.xconfigure.height = measurements.height
        e.xconfigure.border_width = configureRequest.border_width
        e.xconfigure.above = None.convert()
        e.xconfigure.override_redirect = False
        wrapXSendEvent(display, configureRequest.window, False, StructureNotifyMask, e.ptr)
    }

    private fun forwardConfigureRequest(configureRequest: XConfigureRequestEvent) {
        val windowChanges = nativeHeap.alloc<XWindowChanges>()
        windowChanges.x = configureRequest.x
        windowChanges.y = configureRequest.y
        windowChanges.width = configureRequest.width
        windowChanges.height = configureRequest.height
        windowChanges.border_width = configureRequest.border_width
        windowChanges.sibling = configureRequest.above
        windowChanges.stack_mode = configureRequest.detail
        wrapXConfigureWindow(display, configureRequest.window, configureRequest.value_mask.convert(), windowChanges.ptr)
    }
}
