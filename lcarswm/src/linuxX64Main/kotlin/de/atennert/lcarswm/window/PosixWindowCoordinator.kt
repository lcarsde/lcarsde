package de.atennert.lcarswm.window

import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.drawing.UIDrawing
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.events.ReparentEvent
import de.atennert.lcarswm.screen.Screen
import de.atennert.lcarswm.screen.ScreenManager
import de.atennert.lcarswm.system.wrapXConfigureWindow
import de.atennert.lcarswm.system.wrapXSendEvent
import de.atennert.rx.*
import de.atennert.rx.operators.*
import de.atennert.rx.util.Tuple
import gtk.*
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
sealed class WindowToScreenEvent(val window: ManagedWmWindow<Window>, val screen: Screen<RROutput>?)

@ExperimentalForeignApi
class WindowToScreenSetEvent(window: ManagedWmWindow<Window>, screen: Screen<RROutput>) :
    WindowToScreenEvent(window, screen)

@ExperimentalForeignApi
class WindowToScreenRemoveEvent(window: ManagedWmWindow<Window>) : WindowToScreenEvent(window, null)

@ExperimentalForeignApi
private fun Observable<ReparentEvent>.filterForBadParenting(windowList: WindowList) =
    this.withLatestFrom(windowList.windowsObs)
        .filter { (event, windows) ->
            val window = windows.find { it.id == event.id }
            window != null && window.frame != event.parentId
        }
        .map { (event, _) -> event.id }

/**
 *
 */
@ExperimentalForeignApi
class PosixWindowCoordinator(
    eventStore: EventStore,
    screenManager: ScreenManager<RROutput>,
    windowFactory: WindowFactory<Window>,
    windowList: WindowList,
    rootWindowDrawer: UIDrawing,
    private val display: CPointer<Display>?,
) : WindowCoordinator {
    private val logger by inject<Logger>()

    private val windowToScreenEventSj = Subject<WindowToScreenEvent>()
    val windowToScreenEventObs = windowToScreenEventSj.asObservable()

    private val windowsOnScreensSj = BehaviorSubject(emptyMap<ManagedWmWindow<Window>, Screen<*>>())
    val windowsOnScreensObs = windowsOnScreensSj.asObservable()
    private var windowsOnScreens by windowsOnScreensSj

    private val moveWindowToNextScreenSj = Subject<Window>()
    private val moveWindowToPrevScreenSj = Subject<Window>()

    val rearrangeObs = screenManager.screensObs
        .filter { it.isNotEmpty() }
        .map { updatedScreens ->
            val primaryScreen = updatedScreens.find { it.isPrimary } ?: updatedScreens[0]

            val updatedWindows = windowsOnScreens
                .map { (window, screen) ->
                    Pair(window, updatedScreens.getOrElse(updatedScreens.indexOf(screen)) { primaryScreen })
                }
            updatedWindows
        }

    val combinedMeasurementsObs = windowsOnScreensObs
        .map { updatedWindows ->
            updatedWindows.map {
                Observable.of(Tuple(it.value.windowMeasurements, it.value.screenMode, it.key))
            }
        }

    val measurementsObs = combinedMeasurementsObs
        .switchMap { updatedWindows -> Observable.merge(*updatedWindows.toTypedArray()) }

    val titleObs = eventStore.propertyNotifyNameObs
        .withLatestFrom(windowList.windowsObs)
        .map { (windowId, windows) -> windows.find { it.id == windowId } }
        .filterNotNull()

    init {
        val subscription = Subscription()
            .closeWith { this.unsubscribe() }

        subscription.add(
            windowToScreenEventObs
                .subscribe(NextObserver {
                    if (it is WindowToScreenSetEvent) {
                        windowsOnScreens += it.window to it.screen!!
                    } else {
                        windowsOnScreens -= it.window
                    }
                })
        )

        // create and open windows
        subscription.add(
            eventStore.mapObs
                .filter { !windowList.isManaged(it) }
                .bufferWhile(
                    screenManager.primaryScreenObs.filter { it == null },
                    screenManager.primaryScreenObs.filterNotNull()
                )
                .filter { it.isNotEmpty() }
                .withLatestFrom(
                    screenManager.primaryScreenObs
                        .filterNotNull(),
                )
                .subscribe(NextObserver { (windowIds, screen) ->
                    for (windowId in windowIds) {
                        logger.logDebug("PosixWindowCoordinator::init::create window $windowId")
                        windowFactory.createWindow(windowId)
                            ?.let {
                                if (it is ManagedWmWindow) {
                                    logger.logDebug("PosixWindowCoordinator::init::open window $windowId")
                                    windowToScreenEventSj.next(WindowToScreenSetEvent(it, screen))
                                    it.open(screen.windowMeasurements, screen.screenMode)
                                    windowList.add(it)
                                }
                            }
                    }
                })
        )

        // remove windows
        subscription.add(
            eventStore.destroyObs
                .mergeWith(
                    eventStore.unmapObs,
                    eventStore.reparentObs.filterForBadParenting(windowList)
                )
                .withLatestFrom(windowList.windowsObs)
                .subscribe(NextObserver { (windowId, windows) ->
                    windows.find { w -> w.id == windowId }
                        ?.let {
                            logger.logDebug("PosixWindowCoordinator::init::remove window $windowId")
                            windowList.remove(it)
                            windowToScreenEventSj.next(WindowToScreenRemoveEvent(it))
                            it.close()
                        }
                })
        )
        subscription.add(
            eventStore.unmapObs
                .subscribe(NextObserver { rootWindowDrawer.drawWindowManagerFrame() })
        )

        // rearrange windows
        subscription.add(rearrangeObs.subscribe(NextObserver { updatedWindows ->
            windowsOnScreens = updatedWindows.toMap()
        }))

        subscription.add(
            measurementsObs
                .subscribe(NextObserver { (measurements, screenMode, window) ->
                    window.moveResize(measurements, screenMode)
                })
        )
        subscription.add(
            combinedMeasurementsObs
                .subscribe(NextObserver { rootWindowDrawer.drawWindowManagerFrame() })
        )

        // configure window
        subscription.add(
            eventStore.configureRequestObs
                .withLatestFrom(windowList.windowsObs)
                // TODO should filter using windowsOnScreens
                .filter { (configureRequest, windows) ->
                    windows.any { it.id == configureRequest.window && (it !is PosixTransientWindow || !it.isTransientForRoot) }
                }
                .map { (configureRequest) -> configureRequest }
                .switchMap { configureRequest ->
                    Observable.of(configureRequest)
                        .withLatestFrom(Observable.of(windowsOnScreens.firstNotNullOf { entry ->
                            if (entry.key.id == configureRequest.window) {
                                entry.value
                            } else {
                                null
                            }
                        }))
                }
                .subscribe(NextObserver { (configureRequest, screen) ->
                    adjustWindowToScreen(configureRequest, screen.windowMeasurements)
                })
        )

        subscription.add(
            eventStore.configureRequestObs
                .withLatestFrom(windowList.windowsObs)
                // TODO should filter using windowsOnScreens
                .filter { (configureRequest, windows) ->
                    !windows.any { it.id == configureRequest.window && (it !is PosixTransientWindow || !it.isTransientForRoot) }
                }
                .map { (configureRequest) -> configureRequest }
                .subscribe(NextObserver { configureRequest ->
                    forwardConfigureRequest(configureRequest)
                })
        )

        // move windows
        subscription.add(
            moveWindowToNextScreenSj
                .map { windowId -> windowsOnScreens.keys.find { it.id == windowId } }
                .filterNotNull()
                .withLatestFrom(windowsOnScreensObs, screenManager.screensObs)
                .map { (window, windowsOnScreens, screens) ->
                    val currentScreenIndex = screens.indexOf(windowsOnScreens[window])
                    Tuple(window, screens[(currentScreenIndex + 1).rem(screens.size)])
                }
                .subscribe(NextObserver { (window, nextScreen) ->
                    windowToScreenEventSj.next(WindowToScreenSetEvent(window, nextScreen))
                    rootWindowDrawer.drawWindowManagerFrame()
                })
        )

        subscription.add(
            moveWindowToPrevScreenSj
                .map { windowId -> windowsOnScreens.keys.find { it.id == windowId } }
                .filterNotNull()
                .withLatestFrom(windowsOnScreensObs, screenManager.screensObs)
                .map { (window, windowsOnScreens, screens) ->
                    val currentScreenIndex = screens.indexOf(windowsOnScreens[window])
                    Tuple(
                        window,
                        screens[if (currentScreenIndex == 0) screens.size - 1 else currentScreenIndex - 1]
                    )
                }
                .subscribe(NextObserver { (window, prevScreen) ->
                    windowToScreenEventSj.next(WindowToScreenSetEvent(window, prevScreen))
                    rootWindowDrawer.drawWindowManagerFrame()
                })
        )

        // edit window title
        subscription.add(
            titleObs
                .subscribe(NextObserver { window -> window.updateTitle() })
        )
    }

    override fun moveWindowToNextScreen(windowId: Window) {
        moveWindowToNextScreenSj.next(windowId)
    }

    override fun moveWindowToPreviousScreen(windowId: Window) {
        moveWindowToPrevScreenSj.next(windowId)
    }

    override fun moveWindowToScreen(windowId: Window, screen: Screen<RROutput>) {
        val window = windowsOnScreens.keys.single { it.id == windowId }
        windowToScreenEventSj.next(WindowToScreenSetEvent(window, screen))
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
