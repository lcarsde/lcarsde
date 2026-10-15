package de.atennert.lcarswm.window

import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarswm.*
import de.atennert.lcarswm.drivenPorts.drawing.DrawFactory
import de.atennert.lcarswm.drivenPorts.drawing.Surface
import de.atennert.lcarswm.drivenPorts.drawing.TextAlignment
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.keys.KeyManager
import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.settings.GeneralSetting
import de.atennert.lcarswm.system.*
import de.atennert.rx.NextObserver
import de.atennert.rx.Subscription
import de.atennert.rx.operators.filter
import gtk.*
import kotlinx.cinterop.*
import xlib.RROutput

@ExperimentalForeignApi
class PosixButton(
    private val display: CPointer<Display>?,
    screen: Screen,
    monitorManager: MonitorManager<RROutput>,
    keyManager: KeyManager,
    eventStore: EventStore,
    private val text: String,
    private val colorSet: ColorSet,
    private var x: Int,
    private var y: Int,
    private val width: Int,
    private val height: Int,
    settings: Map<GeneralSetting, String>,
    private val onClick: () -> Unit,
) : Button<Window> {
    private val drawFactory by inject<DrawFactory>()

    private val fontFamily = settings[GeneralSetting.FONT]

    override val id: Window = wrapXCreateSimpleWindow(
        display,
        screen.root,
        x,
        y,
        width.convert(),
        height.convert(),
        0.convert(),
        0.convert(),
        0.convert(),
    )

    val cairoSurface = drawFactory.createSurface(
        Surface.Configuration(
            display,
            id,
            screen.root_visual,
            width,
            height,
        )
    )

    private val borderSpace = 4
    private val textH = 11

    private var xOffset = 0

    private val subscription = Subscription()

    private var isHovered = false
    private var isPressed = false

    // TODO combine screen mode and primary monitor handling
    private val screenModeHandler = NextObserver.NextHandler<ScreenMode> {
        when (it) {
            ScreenMode.NORMAL -> {
                xOffset = 0
                changePosition(x, y)
                wrapXMapWindow(display, id)
            }

            ScreenMode.MAXIMIZED -> {
                xOffset = BAR_END_WIDTH + BAR_GAP_SIZE
                changePosition(x + xOffset, y)
                wrapXMapWindow(display, id)
            }

            ScreenMode.FULLSCREEN -> wrapXUnmapWindow(display, id)
        }
    }

    private val primaryMonitorHandler = NextObserver.NextHandler<Monitor<RROutput>?> { monitor ->
        monitor?.let {
            x = it.x
            y = it.y
            this.changePosition(it.x + xOffset, it.y)
        }
    }

    init {
        subscription.add(
            eventStore.enterNotifyObs
                .filter { it == id }
                .subscribe(NextObserver {
                    isHovered = true
                    draw()
                })
        )

        subscription.add(
            eventStore.leaveNotifyObs
                .filter { it == id }
                .subscribe(NextObserver {
                    isHovered = false
                    draw()
                })
        )

        closeWith(PosixButton::cleanup)

        val attributeSet = nativeHeap.alloc<XSetWindowAttributes>()
        attributeSet.event_mask = EnterWindowMask or LeaveWindowMask
        wrapXChangeWindowAttributes(display, id, CWEventMask.convert(), attributeSet.ptr)

        keyManager.grabButton(
            Button1.convert(),
            AnyModifier,
            id,
            (ButtonPressMask or ButtonReleaseMask or ButtonMotionMask).convert(),
            GrabModeAsync,
            None.convert()
        )

        wrapXMapWindow(display, id)

        cairoSurface.setSize(width, height)
        draw()

        subscription.add(
            monitorManager.screenModeObs
                .subscribe(NextObserver(screenModeHandler))
        )

        subscription.add(
            monitorManager.primaryMonitorObs
                .subscribe(NextObserver(primaryMonitorHandler))
        )
    }

    private fun draw() {
        val cr = cairoSurface.createSession()

        val backgroundColor = when {
            isPressed -> colorSet.dark
            isHovered -> colorSet.light
            else -> colorSet.base
        }

        cr.drawRectangle(0, 0, width, height, backgroundColor)

        cr.drawText(
            text,
            fontFamily,
            width - borderSpace,
            height - textH - borderSpace,
            textH,
            TextAlignment.RIGHT,
            BLACK
        )

        cr.dispose()
    }

    override fun changePosition(x: Int, y: Int) {
        wrapXMoveWindow(display, id, x, y)
    }

    override fun press() {
        isPressed = true
        draw()
    }

    override fun release() {
        isPressed = false
        draw()
        onClick()
    }

    private fun cleanup() {
        subscription.unsubscribe()
        cairoSurface.dispose()
        wrapXUnmapWindow(display, id)
        wrapXDestroyWindow(display, id)
    }
}
