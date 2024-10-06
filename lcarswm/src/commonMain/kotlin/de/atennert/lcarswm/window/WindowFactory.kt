package de.atennert.lcarswm.window

import de.atennert.lcarswm.ColorSet

interface WindowFactory<WindowId> {
    fun createButton(
        text: String,
        colorSet: ColorSet,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        onClick: () -> Unit
    ): Button<WindowId>

    fun createWindow(
        id: WindowId,
        isSetup: Boolean = false
    ): WmWindow<WindowId>?
}