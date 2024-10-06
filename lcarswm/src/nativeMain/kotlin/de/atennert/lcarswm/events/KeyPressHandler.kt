package de.atennert.lcarswm.events

import de.atennert.lcarswm.keys.*
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.window.WindowCoordinator
import de.atennert.lcarswm.window.WindowFocusHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.KeyPress
import xlib.XEvent

@ExperimentalForeignApi
class KeyPressHandler(
    private val logger: Logger,
    private val keyManager: KeyManager,
    private val keyConfiguration: KeyConfiguration,
    private val toggleSessionManager: KeySessionManager,
    private val monitorManager: MonitorManager<*>,
    private val windowCoordinator: WindowCoordinator,
    private val windowFocusHandler: WindowFocusHandler
) : XEventHandler {
    override val xEventType = KeyPress

    override fun handleEvent(event: XEvent): Boolean {
        val keyCode = event.xkey.keycode
        val keyMask = keyManager.filterMask(event.xkey.state)

        logger.logDebug("KeyPressHandler::handleEvent::key code: $keyCode, key mask: $keyMask")
        toggleSessionManager.pressKeys(keyCode, keyMask)

        keyManager.getKeySym(keyCode.convert())?.let { keySym ->
            keyConfiguration.getBindingForKey(keySym, keyMask)?.let { keyBinding ->
                when (keyBinding) {
                    is KeyAction -> act(keyBinding.action)
                    else -> {/* so far we only handle key actions here */}
                }
                return false
            }
        }

        return false
    }

    private fun act(action: WmAction): Boolean {
        keyPressSj.next(action)
        when (action) {
            WmAction.WINDOW_MOVE_NEXT -> moveWindowToNextMonitor()
            WmAction.WINDOW_MOVE_PREVIOUS -> moveWindowToPreviousMonitor()
            WmAction.WINDOW_TOGGLE_FWD -> toggleFocusedWindowForward()
            WmAction.WINDOW_TOGGLE_BWD -> toggleFocusedWindowBackward()
            WmAction.SCREEN_MODE_TOGGLE -> toggleScreenMode()
            else -> {/* nothing to do, other actions are handled in key release */}
        }
        return false
    }

    private fun moveWindowToNextMonitor() {
        val focusedWindow = windowFocusHandler.getFocusedWindow() ?: return
        logger.logDebug("KeyPressHandler::moveWindowToNextMonitor::focused window: $focusedWindow")
        windowCoordinator.moveWindowToNextMonitor(focusedWindow)
    }

    private fun moveWindowToPreviousMonitor() {
        val focusedWindow = windowFocusHandler.getFocusedWindow() ?: return
        logger.logDebug("KeyPressHandler::moveWindowToPreviousMonitor::focused window: $focusedWindow")
        windowCoordinator.moveWindowToPreviousMonitor(focusedWindow)
    }

    private fun toggleFocusedWindowForward() {
        windowFocusHandler.toggleWindowFocusForward()
        val newFocusedWindow = windowFocusHandler.getFocusedWindow()
        logger.logDebug("KeyPressHandler::toggleFocusedWindowForward::new focused window: $newFocusedWindow")
    }

    private fun toggleFocusedWindowBackward() {
        windowFocusHandler.toggleWindowFocusBackward()
        val newFocusedWindow = windowFocusHandler.getFocusedWindow()
        logger.logDebug("KeyPressHandler::toggleFocusedWindowBackward::new focused window: $newFocusedWindow")
    }

    private fun toggleScreenMode() {
        logger.logDebug("KeyPressHandler::toggleScreenMode::")
        monitorManager.toggleScreenMode()
    }
}
