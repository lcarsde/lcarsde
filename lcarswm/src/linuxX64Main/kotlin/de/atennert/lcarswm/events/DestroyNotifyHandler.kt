package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.DestroyNotify
import xlib.XEvent

/**
 * The DestroyNotify event is triggered when a window was destroyed. If we (still) know the destroyed window, then we
 * need to clean up after it here.
 */
class DestroyNotifyHandler(
    private val eventStore: EventStore
) : XEventHandler {
    private val logger: Logger by inject()

    @ExperimentalForeignApi
    override val xEventType = DestroyNotify

    @ExperimentalForeignApi
    override fun handleEvent(event: XEvent): Boolean {
        val destroyedWindow = event.xdestroywindow.window
        logger.logDebug("DestroyNotifyHandler::handleEvent::clean up after destroyed window: $destroyedWindow")

        eventStore.destroySj.next(destroyedWindow)

        return false
    }
}