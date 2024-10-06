package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.mouse.MoveWindowManager
import de.atennert.lcarswm.window.Button
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ButtonRelease
import xlib.Window
import xlib.XEvent

@ExperimentalForeignApi
class ButtonReleaseHandler(
    private val logger: Logger,
    private val moveWindowManager: MoveWindowManager,
    private val modeButton: Button<Window>
) : XEventHandler {
    override val xEventType = ButtonRelease

    override fun handleEvent(event: XEvent): Boolean {
        val windowId = event.xbutton.window
        val subWindowId = event.xbutton.subwindow
        logger.logDebug("ButtonReleaseHandler::handleEvent::windowId: $windowId, sub window: $subWindowId, x y: ${event.xbutton.x} ${event.xbutton.y} (${event.xbutton.x_root} ${event.xbutton.y_root})")

        if (windowId == modeButton.id) {
            modeButton.release()
        }

        moveWindowManager.release()
        return false
    }
}