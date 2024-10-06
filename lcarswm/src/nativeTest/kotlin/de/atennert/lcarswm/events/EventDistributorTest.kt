package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalForeignApi
class EventDistributorTest {

    @Test
    fun `handle event`() {
        val eventHandler = TestEventHandler()
        val eventManager = EventDistributor.Builder(LoggerMock())
            .addEventHandler(eventHandler)
            .build()

        val event = nativeHeap.alloc<XEvent>()
        event.type = eventHandler.xEventType

        eventManager.handleEvent(event)

        assertTrue(eventHandler.gotCalled, "The event handler wasn't called by the manager")

        nativeHeap.free(event)
    }

    @Test
    fun `don't handle event`() {
        val eventHandler = TestEventHandler()
        val eventManager = EventDistributor.Builder(LoggerMock())
            .addEventHandler(eventHandler)
            .build()

        val event = nativeHeap.alloc<XEvent>()
        event.type = eventHandler.xEventType + 1

        eventManager.handleEvent(event)

        assertFalse(eventHandler.gotCalled, "The wrong event handler got called")

        nativeHeap.free(event)
    }

    class TestEventHandler : XEventHandler {
        override val xEventType: Int = 42

        var gotCalled = false

        override fun handleEvent(event: XEvent): Boolean {
            this.gotCalled = true
            return false
        }
    }
}