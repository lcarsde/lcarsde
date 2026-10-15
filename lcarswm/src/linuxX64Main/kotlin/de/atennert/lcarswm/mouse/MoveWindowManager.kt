package de.atennert.lcarswm.mouse

import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.screen.Screen
import de.atennert.lcarswm.screen.ScreenManager
import de.atennert.lcarswm.window.ManagedWmWindow
import de.atennert.lcarswm.window.WindowCoordinator
import de.atennert.rx.NextObserver
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

@OptIn(ExperimentalForeignApi::class)
class MoveWindowManager(
    private val windowCoordinator: WindowCoordinator,
    screenManager: ScreenManager<RROutput>
) {
    private val logger by inject<Logger>()

    private var screens = emptyList<Screen<RROutput>>()

    private lateinit var lastWindowScreen: Screen<RROutput>

    private var targetWindow: ManagedWmWindow<Window>? = null

    init {
        screenManager.screensObs.subscribe(NextObserver { screens ->
            // TODO use more RX stuff
            this.screens = screens
            logger.logDebug("MoveWindowManager::updateScreens::$screens")

            // cancel moving the window
            targetWindow?.let {
                logger.logWarning("MoveWindowManager::updateScreens::cancel moving of ${it.id}")
            }
            targetWindow = null
        })
            .closeWith { this.unsubscribe() }
    }

    fun press(window: ManagedWmWindow<Window>, x: Int, y: Int) {
        targetWindow?.let {
            logger.logWarning("MoveWindowManager::press::apparently there was a window moving in process for ${it.id}.")
        }

        targetWindow = window
        lastWindowScreen = getScreen(x, y) ?: return
    }

    fun move(x: Int, y: Int) {
        targetWindow?.let {
            val newScreen = getScreen(x, y) ?: return
            if (newScreen.id != lastWindowScreen.id) {
                windowCoordinator.moveWindowToScreen(it.id, newScreen)
                lastWindowScreen = newScreen
            }
        }
    }

    fun release() {
        targetWindow = null
    }

    private fun getScreen(x: Int, y: Int): Screen<RROutput>? {
        return try {
            screens.first { it.isOnScreen(x, y) }
        } catch (e: Exception) {
            logger.logError(e.toString())
            null
        }
    }
}