package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.monitor.MonitorManagerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import xlib.RRScreenChangeNotify
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExperimentalForeignApi
class RandrScreenChangeHandlerTest {
    @Test
    fun `check correct type of RandrScreenChangeHandler`() {
        val systemApi = SystemFacadeMock()
        val monitorManager = MonitorManagerMock()

        val randrHandlerFactory = RandrHandlerFactory(systemApi, LoggerMock())

        val screenChangeHandler = randrHandlerFactory.createScreenChangeHandler(monitorManager)

        assertEquals(systemApi.randrEventBase + RRScreenChangeNotify,
                screenChangeHandler.xEventType,
                "The factory should create a screen change handler with the appropriate event type")
    }

    @Test
    fun `handle screen change`() {
        val systemApi = SystemFacadeMock()
        val monitorManager = MonitorManagerMock()

        val randrHandlerFactory = RandrHandlerFactory(systemApi, LoggerMock())

        val screenChangeHandler = randrHandlerFactory.createScreenChangeHandler(monitorManager)

        val screenChangeEvent = nativeHeap.alloc<XEvent>()
        screenChangeEvent.type = systemApi.randrEventBase + RRScreenChangeNotify

        val shutdownValue = screenChangeHandler.handleEvent(screenChangeEvent)

        assertFalse(shutdownValue, "Handling a screen change should close the window manager")

        val updateMonitorListCall = monitorManager.functionCalls.removeAt(0)
        assertEquals("updateMonitorList", updateMonitorListCall.name, "The monitor list needs to be updated")
    }
}
