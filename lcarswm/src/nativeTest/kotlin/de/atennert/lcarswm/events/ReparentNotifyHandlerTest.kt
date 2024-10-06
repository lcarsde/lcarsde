package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.ReparentNotify
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExperimentalForeignApi
class ReparentNotifyHandlerTest {
    private val eventStore = EventStore()

    @Test
    fun `handler should have correct type`() {
        assertEquals(ReparentNotify, ReparentNotifyHandler(LoggerMock(), eventStore).xEventType)
    }

    @Test
    fun `send reparent notification`() {
        val events = mutableListOf<ReparentEvent>()
        val subscription = eventStore.reparentObs.subscribe(NextObserver(events::add))

        val reparentEvent = nativeHeap.alloc<XEvent>()
        reparentEvent.type = ReparentNotify
        reparentEvent.xreparent.window = 42.convert()
        reparentEvent.xreparent.parent = 21.convert()

        val reparentNotifyHandler = ReparentNotifyHandler(LoggerMock(), eventStore)

        val shutdownValue = reparentNotifyHandler.handleEvent(reparentEvent)

        assertFalse(shutdownValue, "ReparentNotify should not trigger a shutdown")

        events.shouldContainExactly(listOf(ReparentEvent(42.convert(), 21.convert())))

        nativeHeap.free(reparentEvent.rawPtr)
        subscription.unsubscribe()
    }
}