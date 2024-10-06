package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.DestroyNotify
import xlib.Window
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExperimentalForeignApi
class DestroyNotifyHandlerTest {
    private val eventStore = EventStore()

    @Test
    fun `return the event type DestroyNotify`() {
        val destroyNotifyHandler = DestroyNotifyHandler(LoggerMock(), eventStore)

        assertEquals(DestroyNotify, destroyNotifyHandler.xEventType, "The event type for DestroyEventHandler needs to be DestroyNotify")
    }

    @Test
    fun `send destroy notification`() {
        val destroyWindows = mutableListOf<Window>()
        val subscription = eventStore.destroyObs.subscribe(NextObserver(destroyWindows::add))

        val windowId: Window = 1.convert()

        val destroyNotifyEvent = nativeHeap.alloc<XEvent>()
        destroyNotifyEvent.xdestroywindow.window = windowId

        val destroyNotifyHandler = DestroyNotifyHandler(LoggerMock(), eventStore)
        val requestShutdown = destroyNotifyHandler.handleEvent(destroyNotifyEvent)

        assertFalse(requestShutdown, "Destroy handling should not request shutdown of the window manager")
        destroyWindows.shouldContainExactly(windowId)

        subscription.unsubscribe()
    }
}
