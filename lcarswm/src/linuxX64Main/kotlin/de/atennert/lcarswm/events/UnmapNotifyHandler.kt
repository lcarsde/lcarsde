package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import gtk.UnmapNotify
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Unregister known windows and redraw the root window on unmap notify.
 */
class UnmapNotifyHandler(
    private val eventStore: EventStore
) : XEventHandler {
    private val logger by inject<Logger>()

    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = UnmapNotify

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        val window = event.xunmap.window

        logger.logDebug("UnmapNotifyHandler::handleEvent::unmapped window: $window")
        eventStore.unmapSj.next(window)

        return false
    }
}