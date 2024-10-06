package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.XEvent

/**
 * Distributes events to registered XEventHandlers. Which handler is called depends on
 * the type of the event.
 *
 * Use the EventDistributor.Builder to create this
 */
class EventDistributor private constructor(
    private val eventHandlers: Map<Int, XEventHandler>,
    private val logger: Logger
) {
    /**
     * Process a given event via one of the registered event handlers.
     * @return true if the WM should shut down, false otherwise
     */
    @ExperimentalForeignApi
    fun handleEvent(event: XEvent): Boolean {
        val eventHandler = this.eventHandlers[event.type]

        if (eventHandler == null) {
            logger.logWarning("EventManager::handleEvent::no handler registered for event of type ${event.type}")
            return false
        }

        return eventHandler.handleEvent(event)
    }

    /**
     * Builder for setting up the event distributor.
     */
    class Builder(private val logger: Logger) {
        private val eventHandlers = mutableListOf<XEventHandler>()

        /**
         * Add the required event handlers to the builder for using them
         * with the distributor.
         */
        fun addEventHandler(eventHandler: XEventHandler): Builder {
            this.eventHandlers.add(eventHandler)
            return this
        }

        /**
         * Build the EventDistributor with all registered event handlers.
         */
        fun build(): EventDistributor {
            val mappedEventHandlers = eventHandlers.associateBy { it.xEventType }
            return EventDistributor(mappedEventHandlers, logger)
        }
    }
}