package de.atennert.lcarswm.window

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.atom.TextAtomReader
import de.atennert.lcarswm.conversion.combine
import de.atennert.lcarswm.conversion.toUByteArray
import de.atennert.lcarswm.events.sendConfigureNotify
import de.atennert.lcarswm.keys.KeyManager
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.system.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.toCValues
import xlib.*

@ExperimentalForeignApi
private const val frameEventMask = SubstructureRedirectMask or FocusChangeMask or ButtonPressMask or ButtonReleaseMask

@ExperimentalForeignApi
private val buttonsToGrab = setOf(Button1, Button2, Button3)

@ExperimentalForeignApi
private val wmStateData = listOf<ULong>(NormalState.convert(), None.convert())
    .map { it.toUByteArray() }
    .combine()

@ExperimentalForeignApi
class PosixTransientWindow(
    private val logger: Logger,
    private val display: CPointer<Display>?,
    private val screen: Screen,
    private val atomLibrary: AtomLibrary,
    private val textAtomReader: TextAtomReader,
    private val keyManager: KeyManager,
    override val id: Window,
    private val borderWidth: Int,
    val type: WindowType,
    val transientFor: Window?,
) : ManagedWmWindow<Window> {
    override var frame: Window = 0.convert()
        private set

    override var title: String = TextAtomReader.NO_NAME
        private set

    override var wmClass: String = TextAtomReader.NO_NAME

    val isTransientForRoot = transientFor == null

    private lateinit var measurements: WindowMeasurements

    init {
        wmClass = textAtomReader.readTextProperty(id, Atoms.WM_CLASS)

        updateTitle()
    }

    override fun open(measurements: WindowMeasurements, screenMode: ScreenMode) {
        this.measurements = measurements

        if (!isTransientForRoot) {
            frame = wrapXCreateSimpleWindow(
                display,
                screen.root,
                measurements.x,
                measurements.y,
                measurements.width.convert(),
                measurements.height.convert(),
                0.convert(),
                0.convert(),
                0.convert(),
            )

            logger.logDebug("PosixTransientWindow::open::reparenting $id to $frame")

            wrapXSelectInput(display, frame, frameEventMask)
        }

        wrapXAddToSaveSet(display, id)

        wrapXSetWindowBorderWidth(display, id, 0.convert())

        if (!isTransientForRoot) {
            wrapXReparentWindow(display, id, frame, 0, 0)

            buttonsToGrab.forEach { button ->
                keyManager.grabButton(
                    button.convert(),
                    AnyModifier,
                    id,
                    ButtonPressMask.convert(),
                    GrabModeSync,
                    None.convert()
                )
            }

            wrapXResizeWindow(display, id, measurements.width.convert(), measurements.height.convert())
        }

        wrapXUngrabServer(display)

        if (!isTransientForRoot) {
            wrapXMapWindow(display, frame)
        }

        wrapXMapWindow(display, id)

        if (!isTransientForRoot) {
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
        }
    }

    override fun show() {
    }

    override fun moveResize(measurements: WindowMeasurements, screenMode: ScreenMode) {
        if (isTransientForRoot) {
            return
        }
        this.measurements = measurements

        wrapXMoveResizeWindow(
            display,
            frame,
            measurements.x,
            measurements.y,
            measurements.width.convert(),
            measurements.height.convert()
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
        title = textAtomReader.readTextProperty(id, Atoms.NET_WM_NAME)
        if (title == TextAtomReader.NO_NAME) {
            title = textAtomReader.readTextProperty(id, Atoms.WM_NAME)
        }
    }

    override fun focus() {
    }

    override fun unfocus() {
    }

    override fun hide() {
    }

    override fun close() {
        logger.logDebug("PosixTransientWindow::removeWindow::remove window $id")

        if (!isTransientForRoot) {
            wrapXSelectInput(display, id, NoEventMask)
            buttonsToGrab.forEach {
                keyManager.ungrabButton(it.convert(), AnyModifier, id)
            }

            wrapXUnmapWindow(display, frame)
        }
        wrapXFlush(display)

        wrapXSetWindowBorderWidth(display, id, borderWidth.convert())

        if (!isTransientForRoot) {
            wrapXReparentWindow(display, id, screen.root, 0, 0)
            wrapXDestroyWindow(display, frame)
        }
        wrapXRemoveFromSaveSet(display, id)
    }
}