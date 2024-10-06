package de.atennert.lcarswm.window

import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.system.FunctionCall
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput
import xlib.Window

class WindowCoordinatorMock : WindowCoordinator {
    val functionCalls = mutableListOf<FunctionCall>()

    @ExperimentalForeignApi
    override fun moveWindowToNextMonitor(windowId: Window) {
        functionCalls.add(FunctionCall("moveWindowToNextMonitor", windowId))
    }

    @ExperimentalForeignApi
    override fun moveWindowToPreviousMonitor(windowId: Window) {
        functionCalls.add(FunctionCall("moveWindowToPreviousMonitor", windowId))
    }

    @ExperimentalForeignApi
    override fun moveWindowToMonitor(windowId: Window, monitor: Monitor<RROutput>) {
        TODO("Not yet implemented")
    }
}