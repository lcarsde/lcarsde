package de.atennert.lcarswm.window

import de.atennert.lcarswm.*
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.conversion.combine
import de.atennert.lcarswm.conversion.toUByteArray
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.events.sendConfigureNotify
import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.system.*
import de.atennert.rx.NextObserver
import de.atennert.rx.ReplaySubject
import de.atennert.rx.Subscription
import de.atennert.rx.operators.*
import de.atennert.rx.util.Tuple2
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
class PosixStatusBarWindow(
    private val display: CPointer<Display>?,
    private val rootWindowId: Window,
    monitorManager: MonitorManager<RROutput>,
    eventStore: EventStore,
    atomLibrary: AtomLibrary,
    override val id: Window,
    private val oldBorderWidth: Int,
) : WmWindow<Window> {
    private val wmStateData = listOf<ULong>(NormalState.convert(), None.convert())
        .map { it.toUByteArray() }
        .combine()

    private val windowMeasurementsObs = monitorManager.primaryMonitorObs
        .apply(map(::getWindowMeasurements))

    private val frameIdSj = ReplaySubject<Window>(1)
    private val frameIdObs = frameIdSj.asObservable()

    private val nextHandler = NextObserver.NextHandler<Tuple2<Window, Monitor<RROutput>?>> { (frameId, primaryMonitor) ->
        if (primaryMonitor != null && primaryMonitor.screenMode == ScreenMode.NORMAL) {
            internalShow(frameId)
        } else {
            internalHide(frameId)
        }
    }

    private val subscription = Subscription()

    init {
        subscription.add(windowMeasurementsObs
            .apply(filterNotNull())
            .apply(take(1))
            .apply(map { measurements ->
                wrapXCreateSimpleWindow(
                    display,
                    rootWindowId,
                    measurements.x,
                    measurements.y,
                    measurements.width.convert(),
                    measurements.frameHeight.convert(),
                    0.convert(),
                    0.convert(),
                    0.convert(),
                )
            })
            .subscribe(NextObserver { frameIdSj.next(it) }))

        subscription.add(
            frameIdObs
                .apply(combineLatestWith(windowMeasurementsObs.apply(filterNotNull())))
                .apply(take(1))
                .subscribe(NextObserver { (frameId, measurements) ->
                    wrapXReparentWindow(display, id, frameId, 0, 0)
                    wrapXResizeWindow(display, id, measurements.width.convert(), measurements.height.convert())

                    wrapXUngrabServer(display)

                    val format = 32
                    val bytesPerData = format.div(8)
                    val dataCount = wmStateData.size.div(bytesPerData)
                    wrapXChangeProperty(
                        display,
                        id,
                        atomLibrary[Atoms.WM_STATE],
                        atomLibrary[Atoms.WM_STATE],
                        format,
                        PropModeReplace,
                        wmStateData.toCValues(),
                        dataCount
                    )
                })
        )

        subscription.add(
            frameIdObs
                .apply(combineLatestWith(monitorManager.primaryMonitorObs,))
                .subscribe(NextObserver(nextHandler))
        )

        subscription.add(
            eventStore.destroyObs
                .apply(filter { it == id })
                .apply(switchMap { frameIdObs })
                .subscribe(NextObserver {
                    removeWindow(it)
                })
        )

        subscription.add(
            frameIdObs
                .apply(combineLatestWith(windowMeasurementsObs.apply(filterNotNull())))
                .subscribe(NextObserver { (frameId, measurements) ->
                    internalMoveResize(frameId, measurements)
                })
        )
    }

    override fun open(measurements: WindowMeasurements, screenMode: ScreenMode) {
        // Nothing to do
    }

    override fun show() {
        // Nothing to do
    }

    private fun internalShow(frameId: Window) {
        wrapXMapWindow(display, frameId)
        wrapXMapWindow(display, id)
    }

    override fun moveResize(measurements: WindowMeasurements, screenMode: ScreenMode) {
        // Nothing to do
    }

    private fun internalMoveResize(frameId: Window, measurements: WindowMeasurements) {
        wrapXMoveResizeWindow(
            display,
            frameId,
            measurements.x,
            measurements.y,
            measurements.width.convert(),
            measurements.frameHeight.convert()
        )

        wrapXResizeWindow(
            display,
            id,
            measurements.width.convert(),
            measurements.height.convert()
        )

        sendConfigureNotify(display, id, measurements)
    }

    override fun updateTitle() {
        // Nothing to do
    }

    override fun focus() {
        // Nothing to do
    }

    override fun unfocus() {
        // Nothing to do
    }

    override fun hide() {
        // Nothing to do
    }

    private fun internalHide(frameId: Window) {
        wrapXUnmapWindow(display, id)
        wrapXUnmapWindow(display, frameId)
    }

    override fun close() {
        // Nothing to do
    }

    private fun getWindowMeasurements(primaryMonitor: Monitor<*>?): WindowMeasurements? {
        if (primaryMonitor == null) {
            return null
        }
        return WindowMeasurements(
            primaryMonitor.x + NORMAL_WINDOW_LEFT_OFFSET,
            primaryMonitor.y + BAR_HEIGHT + BAR_GAP_SIZE,
            (primaryMonitor.width - NORMAL_WINDOW_LEFT_OFFSET).convert(),
            (DATA_AREA_HEIGHT).convert(),
            (DATA_AREA_HEIGHT).convert()
        )
    }

    private fun removeWindow(frameId: Window) {
        subscription.unsubscribe()

        wrapXUnmapWindow(display, frameId)
        wrapXFlush(display)

        wrapXSetWindowBorderWidth(display, id, oldBorderWidth.convert())

        wrapXReparentWindow(display, id, rootWindowId, 0, 0)
        wrapXDestroyWindow(display, frameId)
    }

    companion object {
        fun isStatusBar(display: CPointer<Display>?, atomLibrary: AtomLibrary, windowId: Window): Boolean {
            val textProperty = nativeHeap.alloc<XTextProperty>()
            val result =
                wrapXGetTextProperty(display, windowId, textProperty.ptr, atomLibrary[Atoms.LCARSDE_STATUS_BAR])
            wrapXFree(textProperty.value)
            nativeHeap.free(textProperty)
            return result != 0
        }
    }
}