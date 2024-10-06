package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.MapRequest
import xlib.Window
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals


@ExperimentalForeignApi
class MapRequestHandlerTest {
    private val eventStore = EventStore()

    @Test
    fun `has MapRequest type`() {
        val mapRequestHandler = MapRequestHandler(LoggerMock(), eventStore)

        assertEquals(MapRequest, mapRequestHandler.xEventType, "The MapRequestHandler should have the type MapRequest")
    }

    @Test
    fun `handle map events`() {
        val events = mutableListOf<Window>()
        val subscription = eventStore.mapObs.subscribe(NextObserver(events::add))

        val mapRequestHandler = MapRequestHandler(LoggerMock(), eventStore)

        val mapRequestEvent = nativeHeap.alloc<XEvent>()
        mapRequestEvent.type = MapRequest
        mapRequestEvent.xmaprequest.window = 42.convert()

        val shutdownValue = mapRequestHandler.handleEvent(mapRequestEvent)

        assertEquals(false, shutdownValue, "The MapRequestHandler shouldn't trigger a shutdown")

        events.shouldContainExactly(listOf(42.convert()))

        subscription.unsubscribe()
    }
}
