package de.atennert.lcarswm.keys

import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.GrabModeAsync
import kotlin.test.assertEquals

@ExperimentalForeignApi
fun checkGrabKey(
    systemApi: SystemFacadeMock,
    key: String,
    modifiers: List<Modifiers>
) {
    val keyPart = key.split('+').last()

    for (mask in systemApi.lockMasks) {
        val grabKeyCall1 = systemApi.functionCalls.removeAt(0)

        assertEquals("grabKey", grabKeyCall1.name, "Grab key needs to be called to grab $key with ...")
        assertEquals(
            systemApi.keySymKeyCodeMapping[systemApi.keyStrings[keyPart]],
            grabKeyCall1.parameters[0],
            "Grab key needs to be called with the keyCode for $key (modifier ...)"
        )
        assertEquals(
            (getMask(modifiers) or mask).toUInt(),
            grabKeyCall1.parameters[1],
            "The modifier for $key should be ...")
        assertEquals(
            systemApi.rootWindowId,
            grabKeyCall1.parameters[2],
            "The key should be grabbed for the root window"
        )
        assertEquals(
            GrabModeAsync,
            grabKeyCall1.parameters[3],
            "The mode for the grabbed key should be GrabModeAsync"
        )
    }
}
