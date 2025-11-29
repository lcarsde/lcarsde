package de.atennert.lcarswm.window

import de.atennert.lcarswm.atom.TextAtomReader
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.Window

/**
 * POJO for registered windows.
 *
 * @param id Program window ID
 * @param borderWidth Desired program window border width,
 *      we need to remember and reset it to this when we stop handling it
 */
@ExperimentalForeignApi
data class FramedWindow(val id: Window, val borderWidth: Int) {
    /** Frame window ID */
    var frame: Window = 0.convert()

    /** Title bar window ID */
    var titleBar: Window = 0.convert()

    /** Window title (usually depends on program content) */
    var title: String = TextAtomReader.NO_NAME

    /** Name of the program */
    var wmClass: String = TextAtomReader.NO_NAME

    var isTransient: Boolean = false
    var transientFor: Window? = null

    var type: WindowType = WindowType.NORMAL
}
