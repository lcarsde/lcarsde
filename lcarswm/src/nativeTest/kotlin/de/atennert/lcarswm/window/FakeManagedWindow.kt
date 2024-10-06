package de.atennert.lcarswm.window

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.FunctionCall
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.Window

@ExperimentalForeignApi
class FakeManagedWindow(
    override val id: Window = 1.convert(),
    override val frame: Window = 2.convert(),
    override val wmClass: String = "class",
    override val title: String = "title"
) : ManagedWmWindow<Window> {
    val functionCalls = mutableListOf<FunctionCall>()

    override fun open(measurements: WindowMeasurements, screenMode: ScreenMode) {
        functionCalls.add(FunctionCall("open", measurements, screenMode))
    }

    override fun show() {
        functionCalls.add(FunctionCall("show"))
    }

    override fun moveResize(measurements: WindowMeasurements, screenMode: ScreenMode) {
        functionCalls.add(FunctionCall("moveResize", measurements, screenMode))
    }

    override fun updateTitle() {
        functionCalls.add(FunctionCall("updateTitle"))
    }

    override fun focus() {
        functionCalls.add(FunctionCall("focus"))
    }

    override fun unfocus() {
        functionCalls.add(FunctionCall("unfocus"))
    }

    override fun hide() {
        functionCalls.add(FunctionCall("hide"))
    }

    override fun close() {
        functionCalls.add(FunctionCall("close"))
    }
}