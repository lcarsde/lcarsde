package de.atennert.lcarswm.events

import de.atennert.lcarswm.Predicate
import de.atennert.lcarswm.system.api.EventApi
import kotlinx.cinterop.*
import xlib.QueuedAfterFlush
import xlib.Success
import xlib.XEvent

/**
 * Buffers events in an internal queue.
 */
@ExperimentalForeignApi
class EventBuffer(private val eventApi: EventApi) {
    private val internalEventQueue = mutableListOf<CPointer<XEvent>>()

    /**
     * Find an event in the event buffer.
     *
     * @return Pointer to an event, or null if there is no event to be found
     */
    fun findEvent(blocking: Boolean, predicate: Predicate<CPointer<XEvent>>): CPointer<XEvent>? {
        while (true) {
            val eventMatch = internalEventQueue.find(predicate)
            if (eventMatch != null) {
                return eventMatch
            }
            if (!readEvents(blocking)) {
                break
            }
        }
        return null
    }

    /**
     * Get the next event from the buffer.
     *
     * @return Pointer to an event, or null if there is no event to be found
     */
    fun getNextEvent(blocking: Boolean): CPointer<XEvent>? {
        if (internalEventQueue.isEmpty()) {
            readEvents(blocking)
        }
        return if (internalEventQueue.isNotEmpty()) {
            internalEventQueue.removeAt(0)
        } else {
            null
        }
    }

    private fun readEvents(blocking: Boolean): Boolean {
        var n = eventApi.getQueuedEvents(QueuedAfterFlush)
        var alreadyRun = false

        while ((blocking and !alreadyRun) or (n > 0)) {
            val event = nativeHeap.alloc<XEvent>()

            if (eventApi.nextEvent(event.ptr) != Success) {
                nativeHeap.free(event)
                return false
            }

            internalEventQueue.add(event.ptr)

            --n
            alreadyRun = true
        }

        return alreadyRun
    }
}
