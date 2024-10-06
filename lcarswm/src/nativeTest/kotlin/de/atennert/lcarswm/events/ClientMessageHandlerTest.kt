package de.atennert.lcarswm.events

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import xlib.ClientMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExperimentalForeignApi
class ClientMessageHandlerTest {
    @Test
    fun `check correct type`() {
        assertEquals(ClientMessage, ClientMessageHandler(LoggerMock(), AtomLibrary(SystemFacadeMock())).xEventType, "The message handler should have the correct type")
    }

    @Test
    fun `check not shutting down`() {
        assertFalse(ClientMessageHandler(LoggerMock(), AtomLibrary(SystemFacadeMock())).handleEvent(nativeHeap.alloc()), "The message handler should not trigger a shutdown of the WM")
    }
}