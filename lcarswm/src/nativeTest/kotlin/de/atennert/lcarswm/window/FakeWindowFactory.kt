package de.atennert.lcarswm.window

import de.atennert.lcarswm.ColorSet
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window

@ExperimentalForeignApi
class FakeWindowFactory : WindowFactory<Window> {
    override fun createButton(
        text: String,
        colorSet: ColorSet,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        onClick: () -> Unit
    ): Button<Window> {
        TODO("Not yet implemented")
    }

    override fun createWindow(id: Window, isSetup: Boolean): WmWindow<Window> {
        return FakeManagedWindow(id = id)
    }
}