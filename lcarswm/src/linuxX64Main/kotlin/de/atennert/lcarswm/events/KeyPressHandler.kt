package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.keys.*
import de.atennert.lcarswm.screen.ScreenManager
import de.atennert.lcarswm.window.WindowCoordinator
import de.atennert.lcarswm.window.WindowFocusHandler
import gtk.KeyPress
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert

@ExperimentalForeignApi
class KeyPressHandler(
    private val keyManager: KeyManager,
    private val keyConfiguration: KeyConfiguration,
    private val windowCyclingManager: WindowCyclingManager,
    private val screenManager: ScreenManager<*>,
    private val windowCoordinator: WindowCoordinator,
    private val windowFocusHandler: WindowFocusHandler
) : XEventHandler {
    private val logger: Logger by inject()

    override val xEventType = KeyPress

    override fun handleEvent(event: XEvent): Boolean {
        val keyCode = event.xkey.keycode
        val keyMask = keyManager.filterMask(event.xkey.state)

        logger.logDebug("KeyPressHandler::handleEvent::key code: $keyCode, key mask: $keyMask")
        windowCyclingManager.pressKeys(keyCode, keyMask)

        keyManager.getKeySym(keyCode.convert())?.let { keySym ->
            keyConfiguration.getBindingForKey(keySym, keyMask)?.let { keyBinding ->
                when (keyBinding) {
                    is KeyAction -> act(keyBinding.action)
                    else -> {/* so far we only handle key actions here */
                    }
                }
                return false
            }
        }

        return false
    }

    private fun act(action: WmAction): Boolean {
        keyPressSj.next(action)
        when (action) {
            WmAction.WINDOW_MOVE_NEXT -> moveWindowToNextScreen()
            WmAction.WINDOW_MOVE_PREVIOUS -> moveWindowToPreviousScreen()
            WmAction.WINDOW_TOGGLE_FWD -> toggleFocusedWindowForward()
            WmAction.WINDOW_TOGGLE_BWD -> toggleFocusedWindowBackward()
            WmAction.SCREEN_MODE_TOGGLE -> toggleScreenMode()
            else -> {/* nothing to do, other actions are handled in key release */
            }
        }
        return false
    }

    private fun moveWindowToNextScreen() {
        val focusedWindow = windowFocusHandler.getFocusedWindow() ?: return
        logger.logDebug("KeyPressHandler::moveWindowToNextScreen::focused window: $focusedWindow")
        windowCoordinator.moveWindowToNextScreen(focusedWindow)
    }

    private fun moveWindowToPreviousScreen() {
        val focusedWindow = windowFocusHandler.getFocusedWindow() ?: return
        logger.logDebug("KeyPressHandler::moveWindowToPreviousScreen::focused window: $focusedWindow")
        windowCoordinator.moveWindowToPreviousScreen(focusedWindow)
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
        screenManager.toggleScreenMode()
    }
}
