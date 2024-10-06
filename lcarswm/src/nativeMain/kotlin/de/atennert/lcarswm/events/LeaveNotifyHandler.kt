package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.LeaveNotify
import xlib.XEvent

class LeaveNotifyHandler(
    private val logger: Logger,
    private val eventStore: EventStore,
) : XEventHandler {
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