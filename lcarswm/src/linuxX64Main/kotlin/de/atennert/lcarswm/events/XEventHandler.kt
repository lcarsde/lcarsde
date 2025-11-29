package de.atennert.lcarswm.events

import kotlinx.cinterop.ExperimentalForeignApi
import xlib.XEvent

/**
 * Interface for handlers of X events.
 */
interface XEventHandler {
    /** The handled X event type */
    val xEventType: Int

    /**
     * @return true if event evaluation and WM shall stop, false otherwise
     */
    @OptIn(ExperimentalForeignApi::class)
    fun handleEvent(event: XEvent): Boolean
}