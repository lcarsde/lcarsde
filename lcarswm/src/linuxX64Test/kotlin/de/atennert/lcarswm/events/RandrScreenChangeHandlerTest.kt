package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.screen.ScreenManagerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import xlib.RRScreenChangeNotify
import kotlin.test.*

@ExperimentalForeignApi
class RandrScreenChangeHandlerTest {
    @BeforeTest
    fun setup() {
        ServiceLocator.provide<Logger> { LoggerMock() }
    }

    @AfterTest
    fun teardown() {
        ServiceLocator.clear()
    }

    @Test
    fun `check correct type of RandrScreenChangeHandler`() {
        val systemApi = SystemFacadeMock()
        val screenManager = ScreenManagerMock()

        val randrHandlerFactory = RandrHandlerFactory(systemApi)

        val screenChangeHandler = randrHandlerFactory.createScreenChangeHandler(screenManager)

        assertEquals(
            systemApi.randrEventBase + RRScreenChangeNotify,
            screenChangeHandler.xEventType,
            "The factory should create a screen change handler with the appropriate event type"
        )
    }

    @Test
    fun `handle screen change`() {
        val systemApi = SystemFacadeMock()
        val screenManager = ScreenManagerMock()

        val randrHandlerFactory = RandrHandlerFactory(systemApi)

        val screenChangeHandler = randrHandlerFactory.createScreenChangeHandler(screenManager)

        val screenChangeEvent = nativeHeap.alloc<XEvent>()
        screenChangeEvent.type = systemApi.randrEventBase + RRScreenChangeNotify

        val shutdownValue = screenChangeHandler.handleEvent(screenChangeEvent)

        assertFalse(shutdownValue, "Handling a screen change should close the window manager")

        val updateScreenListCall = screenManager.functionCalls.removeAt(0)
        assertEquals(
            "updateScreenList",
            updateScreenListCall.name,
            "The screen list needs to be updated"
        )
    }
}
