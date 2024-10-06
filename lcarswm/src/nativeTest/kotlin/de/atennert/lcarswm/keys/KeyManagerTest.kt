package de.atennert.lcarswm.keys

import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.ControlMask
import xlib.LockMask
import xlib.ShiftMask
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalForeignApi
class KeyManagerTest {
    @Test
    fun `ungrab all keys`() {
        val systemApi = SystemFacadeMock()

        KeyManager(systemApi).ungrabAllKeys(systemApi.rootWindowId)

        val ungrabKeysCall = systemApi.functionCalls.removeAt(0)

        assertEquals("ungrabKey", ungrabKeysCall.name, "The keys need to be initially ungrabbed")
        assertEquals(systemApi.rootWindowId, ungrabKeysCall.parameters[0], "The keys need to be ungrabbed for the root window")
    }

    @Test
    fun `load modifier keys`() {
        val systemApi = SystemFacadeMock()

        val keyManager = KeyManager(systemApi)

        val expectedModifiers = mutableMapOf(
            Modifiers.CAPS_LOCK to LockMask,
            Modifiers.SHIFT to ShiftMask,
            Modifiers.CONTROL to ControlMask,
            Modifiers.ALT to 0x8,
            Modifiers.HYPER to 0x10,
            Modifiers.META to 0x20,
            Modifiers.SUPER to 0x40,
            Modifiers.SCROLL_LOCK to 0x80,
        )

        assertEquals(expectedModifiers, keyManager.modMasks, "The KeyManager should get the required modifier keys")
    }

    @Test
    fun `return null on unknown key`() {
        val systemApi = SystemFacadeMock()

        val keyManager = KeyManager(systemApi)

        assertNull(keyManager.getKeySym((-1).convert()), "The key manager should return null for unknown key codes")
    }
}