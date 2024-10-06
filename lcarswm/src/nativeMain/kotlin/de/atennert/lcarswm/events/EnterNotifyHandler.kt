package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.EnterNotify
import xlib.XEvent

class EnterNotifyHandler(
    private val logger: Logger,
    private val eventStore: EventStore,
) : XEventHandler {
    @ExperimentalForeignApi
    override val xEventType = EnterNotify

    @ExperimentalForeignApi
    override fun handleEvent(event: XEvent): Boolean {
        val enterEvent = event.xcrossing
        logger.logDebug("EnterNotifyHandler::handleEvent::window: ${enterEvent.window}, sub-window: ${enterEvent.subwindow}")

        eventStore.enterNotifySj.next(enterEvent.window)

        return false
    }
}