package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.mouse.MoveWindowManager
import de.atennert.lcarswm.system.api.InputApi
import de.atennert.lcarswm.window.Button
import de.atennert.lcarswm.window.PosixTransientWindow
import de.atennert.lcarswm.window.WindowFocusHandler
import de.atennert.lcarswm.window.WindowList
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ButtonPress
import xlib.ReplayPointer
import xlib.Window
import xlib.XEvent

@ExperimentalForeignApi
class ButtonPressHandler(
    private val logger: Logger,
    private val inputApi: InputApi,
    private val windowList: WindowList,
    private val focusHandler: WindowFocusHandler,
    private val moveWindowManager: MoveWindowManager,
    private val modeButton: Button<Window>
) : XEventHandler {
    override val xEventType = ButtonPress

    override fun handleEvent(event: XEvent): Boolean {
        val windowId = event.xbutton.window
        val subWindowId = event.xbutton.subwindow
        logger.logDebug("ButtonPressHandler::handleEvent::windowId: $windowId, sub window: $subWindowId, x y: ${event.xbutton.x} ${event.xbutton.y} (${event.xbutton.x_root} ${event.xbutton.y_root})")

        if (windowId == modeButton.id) {
            modeButton.press()
            return false
        }

        windowList.getByAny(windowId)?.let { window ->
            if (window is PosixTransientWindow) {
                return@let
            }
            logger.logDebug("handle focus")
            focusHandler.setFocusedWindow(window.id)

            if (window.isTitleBar(windowId)) {
                moveWindowManager.press(window, event.xbutton.x_root, event.xbutton.y_root)
            }
        }

        windowList.get(windowId)?.let {
            logger.logDebug("replay event")
            inputApi.allowEvents(ReplayPointer, event.xbutton.time)
        }
        return false
    }
}