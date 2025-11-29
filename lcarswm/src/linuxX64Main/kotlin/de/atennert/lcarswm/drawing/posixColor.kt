package de.atennert.lcarswm.drawing

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.XColor

/**
 * Creates an XColor instance.
 *
 * THIS NEEDS TO BE CLEANED UP WITH nativeHeap.free(...)!!!
 */
@ExperimentalForeignApi
fun Color.toXColor(): XColor {
    val xColor = nativeHeap.alloc<XColor>()
    xColor.red = redOp.convert()
    xColor.green = greenOp.convert()
    xColor.blue = blueOp.convert()
    return xColor
}
