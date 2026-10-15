package de.atennert.lcarswm.window

import de.atennert.lcarswm.screen.Screen
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

/**
 * Interface for class that manages the mapping of windows to screens
 */
interface WindowCoordinator {
    @ExperimentalForeignApi
    fun moveWindowToNextScreen(windowId: Window)

    @ExperimentalForeignApi
    fun moveWindowToPreviousScreen(windowId: Window)

    @ExperimentalForeignApi
    fun moveWindowToScreen(windowId: Window, screen: Screen<RROutput>)
}
