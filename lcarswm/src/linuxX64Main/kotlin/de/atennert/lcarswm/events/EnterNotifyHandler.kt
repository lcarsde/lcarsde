package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import gtk.EnterNotify
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

class EnterNotifyHandler(
    private val eventStore: EventStore,
) : XEventHandler {
    private val logger by inject<Logger>()

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