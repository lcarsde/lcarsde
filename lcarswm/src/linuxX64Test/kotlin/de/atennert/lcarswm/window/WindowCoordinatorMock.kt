package de.atennert.lcarswm.window

import de.atennert.lcarswm.screen.Screen
import de.atennert.lcarswm.system.FunctionCall
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

class WindowCoordinatorMock : WindowCoordinator {
    val functionCalls = mutableListOf<FunctionCall>()

    @ExperimentalForeignApi
    override fun moveWindowToNextScreen(windowId: Window) {
        functionCalls.add(
            FunctionCall(
                "moveWindowToNextScreen",
                windowId
            )
        )
    }

    @ExperimentalForeignApi
    override fun moveWindowToPreviousScreen(windowId: Window) {
        functionCalls.add(
            FunctionCall(
                "moveWindowToPreviousScreen",
                windowId
            )
        )
    }

    @ExperimentalForeignApi
    override fun moveWindowToScreen(windowId: Window, screen: Screen<RROutput>) {
        TODO("Not yet implemented")
    }
}