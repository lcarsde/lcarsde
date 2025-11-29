package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import xlib.ClientMessage
import kotlin.test.*

@ExperimentalForeignApi
class ClientMessageHandlerTest {
    @BeforeTest
    fun setup() {
        ServiceLocator.provide<Logger> { LoggerMock() }
    }

    @AfterTest
    fun teardown() {
        ServiceLocator.clear()
    }

    @Test
    fun `check correct type`() {
        assertEquals(
            ClientMessage,
            ClientMessageHandler(AtomLibrary(SystemFacadeMock())).xEventType,
            "The message handler should have the correct type"
        )
    }

    @Test
    fun `check not shutting down`() {
        assertFalse(
            ClientMessageHandler(AtomLibrary(SystemFacadeMock())).handleEvent(
                nativeHeap.alloc()
            ),
            "The message handler should not trigger a shutdown of the WM"
        )
    }
}