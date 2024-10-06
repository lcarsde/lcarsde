package de.atennert.lcarswm.window

import de.atennert.lcarswm.system.FunctionCall
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.StructureNotifyMask
import kotlin.test.assertEquals

@ExperimentalForeignApi
fun checkMoveWindow(
    systemCalls: MutableList<FunctionCall>,
    window: FramedWindow
) {
    val moveResizeTitleBarCall = systemCalls.removeAt(0)
    assertEquals("moveResizeWindow", moveResizeTitleBarCall.name, "The title bar needs to be moved/resized")
    assertEquals(window.titleBar, moveResizeTitleBarCall.parameters[0], "The _title bar_ needs to be moved/resized")

    val moveResizeFrameCall = systemCalls.removeAt(0)
    assertEquals("moveResizeWindow", moveResizeFrameCall.name, "The frame needs to be moved/resized")
    assertEquals(window.frame, moveResizeFrameCall.parameters[0], "The _frame_ needs to be moved/resized")

    val resizeWindowCall = systemCalls.removeAt(0)
    assertEquals("resizeWindow", resizeWindowCall.name, "The window needs to be resized")
    assertEquals(window.id, resizeWindowCall.parameters[0], "The _window_ needs to be resized")

    val sendEventCall = systemCalls.removeAt(0)
    assertEquals("sendEvent", sendEventCall.name, "The window needs to get a structure notify event")
    assertEquals(window.id, sendEventCall.parameters[0], "The _window_ needs to get a structure notify event")
    assertEquals(
        StructureNotifyMask,
        sendEventCall.parameters[2],
        "The window needs to get a _structure notify_ event"
    )
}
