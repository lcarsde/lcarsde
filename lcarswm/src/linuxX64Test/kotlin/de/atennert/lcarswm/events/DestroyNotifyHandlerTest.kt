package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
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
import kotlin.test.*

@ExperimentalForeignApi
class DestroyNotifyHandlerTest {
    private val eventStore = EventStore()

    @BeforeTest
    fun setup() {
        ServiceLocator.provide<Logger> { LoggerMock() }
    }

    @AfterTest
    fun teardown() {
        ServiceLocator.clear()
    }

    @Test
    fun `return the event type DestroyNotify`() {
        val destroyNotifyHandler = DestroyNotifyHandler(eventStore)

        assertEquals(
            DestroyNotify,
            destroyNotifyHandler.xEventType,
            "The event type for DestroyEventHandler needs to be DestroyNotify"
        )
    }

    @Test
    fun `send destroy notification`() {
        val destroyWindows = mutableListOf<Window>()
        val subscription = eventStore.destroyObs.subscribe(NextObserver(destroyWindows::add))

        val windowId: Window = 1.convert()

        val destroyNotifyEvent = nativeHeap.alloc<XEvent>()
        destroyNotifyEvent.xdestroywindow.window = windowId

        val destroyNotifyHandler = DestroyNotifyHandler(eventStore)
        val requestShutdown = destroyNotifyHandler.handleEvent(destroyNotifyEvent)

        assertFalse(requestShutdown, "Destroy handling should not request shutdown of the window manager")
        destroyWindows.shouldContainExactly(windowId)

        subscription.unsubscribe()
    }
}
