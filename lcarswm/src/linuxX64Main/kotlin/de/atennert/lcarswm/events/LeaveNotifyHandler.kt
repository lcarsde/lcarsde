package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import gtk.LeaveNotify
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

class LeaveNotifyHandler(
    private val eventStore: EventStore,
) : XEventHandler {
    private val logger by inject<Logger>()

    @ExperimentalForeignApi
    override val xEventType = LeaveNotify

    @ExperimentalForeignApi
    override fun handleEvent(event: XEvent): Boolean {
        val leaveEvent = event.xcrossing
        logger.logDebug("LeaveNotifyHandler::handleEvent::window: ${leaveEvent.window}, sub-window: ${leaveEvent.subwindow}")

        eventStore.leaveNotifySj.next(leaveEvent.window)

        return false
    }
}