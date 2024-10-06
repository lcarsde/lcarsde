package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ConfigureRequest
import xlib.XEvent

/**
 * Depending on whether we know or don't know the window of the configure request event, we adjust the windows
 * dimensions or simply forward the request.
 */
class ConfigureRequestHandler(
    private val logger: Logger,
    private val eventStore: EventStore,
) : XEventHandler {
    @ExperimentalForeignApi
    override val xEventType = ConfigureRequest

    @ExperimentalForeignApi
    override fun handleEvent(event: XEvent): Boolean {
        val configureEvent = event.xconfigurerequest

        logger.logDebug("ConfigureRequestHandler::handleEvent::configure request for window ${configureEvent.window}, stack mode: ${configureEvent.detail}, sibling: ${configureEvent.above}, parent: ${configureEvent.parent}")

        eventStore.configureRequestSj.next(configureEvent)

        return false
    }
}