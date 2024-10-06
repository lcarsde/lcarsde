package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.mouse.MoveWindowManager
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.MotionNotify
import xlib.XEvent

class MotionNotifyHandler(
    private val logger: Logger,
    private val moveWindowManager: MoveWindowManager
) : XEventHandler {
    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = MotionNotify

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        val windowId = event.xmotion.window
        val subWindowId = event.xmotion.subwindow
        logger.logDebug("MotionNotifyHandler::handleEvent::windowId: $windowId, sub window: $subWindowId, x y: ${event.xmotion.x} ${event.xmotion.y} (${event.xmotion.x_root} ${event.xmotion.y_root})")

        moveWindowManager.move(event.xmotion.x_root, event.xmotion.y_root)
        return false
    }
}