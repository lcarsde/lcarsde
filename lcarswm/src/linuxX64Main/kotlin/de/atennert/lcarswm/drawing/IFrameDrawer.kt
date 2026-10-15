package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.window.WindowMeasurements
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi

interface IFrameDrawer {
    @ExperimentalForeignApi
    fun drawFrame(
        measurements: WindowMeasurements,
        screenMode: ScreenMode,
        isFocused: Boolean,
        title: String,
        titleBar: Window
    )
}