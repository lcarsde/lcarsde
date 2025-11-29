package de.atennert.lcarswm.system.api

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window
import xlib.XEvent

/**
 *
 */
@ExperimentalForeignApi
interface EventApi {

    fun sync(discardQueuedEvents: Boolean): Int

    fun sendEvent(window: Window, propagate: Boolean, eventMask: Long, event: CPointer<XEvent>): Int

    fun nextEvent(event: CPointer<XEvent>): Int

    fun getQueuedEvents(mode: Int): Int

    fun lowerWindow(window: Window): Int

    fun mapWindow(window: Window): Int

    fun destroyWindow(window: Window): Int
}