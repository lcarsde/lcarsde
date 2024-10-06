package de.atennert.lcarswm.events

import de.atennert.lcarswm.Environment
import de.atennert.lcarswm.keys.*
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemCallMocker
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import xlib.MappingNotify
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalForeignApi
class MappingNotifyHandlerTest : SystemCallMocker() {

    private val keySetting = setOf(
        KeyExecution("Ctrl+F4", "command arg1 arg2")
    )

    @Test
    fun `return the event type MappingNotify`() {
        val system = SystemFacadeMock()
        val env = Environment(system.display)
        val keyManager = KeyManager(system)
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            system,
            keySetting,
            keyManager,
            keySessionManager,
            system.rootWindowId
        )
        val mappingNotifyHandler = MappingNotifyHandler(LoggerMock(), keyManager, keyConfiguration, system.rootWindowId)

        assertEquals(MappingNotify, mappingNotifyHandler.xEventType, "The MappingNotifyHandler should have the correct type")
    }

    @Test
    fun `reload the key bindings`() {
        val system = SystemFacadeMock()
        val env = Environment(system.display)
        val keyManager = KeyManager(system)
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            system,
            keySetting,
            keyManager,
            keySessionManager,
            system.rootWindowId
        )
        val mappingNotifyHandler = MappingNotifyHandler(LoggerMock(), keyManager, keyConfiguration, system.rootWindowId)

        system.functionCalls.clear()

        val mappingNotifyEvent = nativeHeap.alloc<XEvent>()
        mappingNotifyEvent.type = MappingNotify

        mappingNotifyHandler.handleEvent(mappingNotifyEvent)

        assertEquals("ungrabKey", system.functionCalls.removeAt(0).name, "ungrab keys")
        assertEquals("freeModifiermap", system.functionCalls.removeAt(0).name, "free the known modifiers")
        assertEquals("xFree", system.functionCalls.removeAt(0).name, "free the key map")

        keySetting.forEach { setting ->
            checkGrabKey(system, setting.keys, listOf(Modifiers.CONTROL))
        }
        assertTrue(system.functionCalls.isEmpty(), "There should be no more system calls")
    }
}