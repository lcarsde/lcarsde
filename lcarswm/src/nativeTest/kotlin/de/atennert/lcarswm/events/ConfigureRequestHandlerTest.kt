package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.*
import xlib.ConfigureRequest
import xlib.Window
import xlib.XConfigureRequestEvent
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 *
 */
@ExperimentalForeignApi
class ConfigureRequestHandlerTest {
    private val eventStore = EventStore()

    @Test
    fun `has ConfigureRequest type`() {
        val configureRequestHandler = ConfigureRequestHandler(LoggerMock(), eventStore)

        assertEquals(ConfigureRequest, configureRequestHandler.xEventType, "The ConfigureRequestHandler should have the ConfigureRequest type")
    }

    @Test
    fun `forward the request`() {
        val events = mutableListOf<XConfigureRequestEvent>()
        val subscription = eventStore.configureRequestObs.subscribe(NextObserver(events::add))

        val configureRequestHandler = ConfigureRequestHandler(LoggerMock(), eventStore)

        val configureRequestEvent = createConfigureRequestEvent(42.convert())
        val shutdownValue = configureRequestHandler.handleEvent(configureRequestEvent)

        assertFalse(shutdownValue, "The ConfigureRequestHandler shouldn't trigger a shutdown")

        events.last().window.shouldBe(configureRequestEvent.xconfigurerequest.window)
        events.last().value_mask.shouldBe(configureRequestEvent.xconfigurerequest.value_mask)
        events.last().x.shouldBe(configureRequestEvent.xconfigurerequest.x)
        events.last().y.shouldBe(configureRequestEvent.xconfigurerequest.y)
        events.last().width.shouldBe(configureRequestEvent.xconfigurerequest.width)
        events.last().height.shouldBe(configureRequestEvent.xconfigurerequest.height)
        events.last().border_width.shouldBe(configureRequestEvent.xconfigurerequest.border_width)
        events.last().above.shouldBe(configureRequestEvent.xconfigurerequest.above)
        events.last().detail.shouldBe(configureRequestEvent.xconfigurerequest.detail)

        nativeHeap.free(configureRequestEvent)
        subscription.unsubscribe()
    }

    private fun createConfigureRequestEvent(windowId: Window): XEvent {
        val configureRequestEvent = nativeHeap.alloc<XEvent>()
        configureRequestEvent.xconfigurerequest.window = windowId
        configureRequestEvent.xconfigurerequest.value_mask = 123.convert()
        configureRequestEvent.xconfigurerequest.x = 2
        configureRequestEvent.xconfigurerequest.y = 3
        configureRequestEvent.xconfigurerequest.width = 4
        configureRequestEvent.xconfigurerequest.height = 5
        configureRequestEvent.xconfigurerequest.border_width = 10
        configureRequestEvent.xconfigurerequest.above = 6.convert()
        configureRequestEvent.xconfigurerequest.detail = 7
        return configureRequestEvent
    }
}