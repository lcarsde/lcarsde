package de.atennert.lcarswm.events

import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.*
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalForeignApi
class EventBufferTest {
    @Test
    fun `get available events`() {
        val events = listOf(1, 2, 3)
            .map {
                val event = nativeHeap.alloc<XEvent>()
                event.type = it
                event
            }
        val system = object : SystemFacadeMock() {
            val sysEvents = events.toMutableList()
            override fun nextEvent(event: CPointer<XEvent>): Int {
                event.pointed.type = sysEvents.removeAt(0).type
                return 0
            }

            override fun getQueuedEvents(mode: Int): Int = sysEvents.size
        }

        val eventBuffer = EventBuffer(system)

        events.forEach { testEvent ->
            assertEquals(
                testEvent.type,
                eventBuffer.findEvent(false)
                { bufferedEvent -> bufferedEvent.pointed.type == testEvent.type }?.pointed?.type,
                "Unable to find element with type ${testEvent.type}"
            )
        }

        events.forEach { testEvent ->
            assertEquals(
                testEvent.type,
                eventBuffer.getNextEvent(true)?.pointed?.type,
                "Unable to get element with type ${testEvent.type}"
            )
        }
    }
}