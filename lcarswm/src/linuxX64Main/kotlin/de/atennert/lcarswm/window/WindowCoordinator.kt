package de.atennert.lcarswm.window

import de.atennert.lcarswm.monitor.Monitor
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput
import xlib.Window

/**
 * Interface for class that manages the mapping of windows to monitors
 */
interface WindowCoordinator {
    @ExperimentalForeignApi
    fun moveWindowToNextMonitor(windowId: Window)

    @ExperimentalForeignApi
    fun moveWindowToPreviousMonitor(windowId: Window)

    @ExperimentalForeignApi
    fun moveWindowToMonitor(windowId: Window, monitor: Monitor<RROutput>)
}
