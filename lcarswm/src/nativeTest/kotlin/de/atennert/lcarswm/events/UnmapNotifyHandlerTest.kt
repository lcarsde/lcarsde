package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.UnmapNotify
import xlib.Window
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 *
 */
@ExperimentalForeignApi
class UnmapNotifyHandlerTest {
    private val eventStore = EventStore()

    @Test
    fun `return correct message type`() {
        val unmapNotifyHandler = UnmapNotifyHandler(LoggerMock(), eventStore)

        assertEquals(UnmapNotify, unmapNotifyHandler.xEventType, "UnmapNotifyHandler should have type UnmapNotify")
    }

    @Test
    fun `unregister managed window`() {
        val events = mutableListOf<Window>()
        val subscription = eventStore.unmapObs.subscribe(NextObserver(events::add))

        val unmapNotifyHandler = UnmapNotifyHandler(LoggerMock(), eventStore)

        val unmapEvent = nativeHeap.alloc<XEvent>()
        unmapEvent.type = UnmapNotify
        unmapEvent.xunmap.window = 42.convert()

        val shutdownValue = unmapNotifyHandler.handleEvent(unmapEvent)

        assertFalse(shutdownValue, "The unmap handling shouldn't trigger a shutdown")

        events.shouldContainExactly(listOf(42.convert()))

        nativeHeap.free(unmapEvent.rawPtr)
        subscription.unsubscribe()
    }
}