package de.atennert.lcarswm.events

import de.atennert.lcarswm.system.wrapXSendEvent
import de.atennert.lcarswm.window.WindowMeasurements
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
fun sendConfigureNotify(
    display: CPointer<Display>?,
    window: Window,
    measurements: WindowMeasurements
) {
    val e = nativeHeap.alloc<XEvent>()
    e.type = ConfigureNotify
    e.xconfigure.event = window
    e.xconfigure.window = window
    e.xconfigure.x = measurements.x
    e.xconfigure.y = measurements.y
    e.xconfigure.width = measurements.width
    e.xconfigure.height = measurements.height
    e.xconfigure.border_width = 0
    e.xconfigure.above = None.convert()
    e.xconfigure.override_redirect = False
    wrapXSendEvent(display, window, False, StructureNotifyMask, e.ptr)
}
