package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.window.WindowMeasurements
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Colormap
import xlib.Window

interface IFrameDrawer {
    @ExperimentalForeignApi
    val colorMap: Colormap
    @ExperimentalForeignApi
    fun drawFrame(measurements: WindowMeasurements, screenMode: ScreenMode, isFocused: Boolean, title: String, titleBar: Window)
}