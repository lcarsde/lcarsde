package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import gtk.MapRequest
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

/**
 *
 */
class MapRequestHandler(
    private val eventStore: EventStore
) : XEventHandler {
    private val logger: Logger by inject()

    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = MapRequest

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        val mapEvent = event.xmaprequest
        logger.logDebug("MapRequestHandler::handleEvent::map request for window ${mapEvent.window}, parent: ${mapEvent.parent}")

        eventStore.mapSj.next(mapEvent.window)

        return false
    }
}
