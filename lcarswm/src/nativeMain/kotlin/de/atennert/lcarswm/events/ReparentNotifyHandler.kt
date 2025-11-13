package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ReparentNotify
import xlib.XEvent

class ReparentNotifyHandler(
    private val eventStore: EventStore
) : XEventHandler {
    private val logger by inject<Logger>()

    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = ReparentNotify

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        val windowId = event.xreparent.window
        val parentId = event.xreparent.parent
        logger.logDebug("ReparentNotifyHandler::handleEvent::reparented $windowId to $parentId")
        eventStore.reparentSj.next(ReparentEvent(windowId, parentId))

        return false
    }
}
