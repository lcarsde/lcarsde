package de.atennert.lcarswm.keys

import de.atennert.lcarswm.system.api.InputApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.AnyModifier
import xlib.KeySym
import xlib.Window

/**
 * Loads the key configuration of the users and provides the corresponding key bindings.
 */
@ExperimentalForeignApi
class KeyConfiguration(
    private val inputApi: InputApi,
    private val keyConfiguration: Set<KeyBinding>,
    private val keyManager: KeyManager,
    private val toggleSessionManager: KeySessionManager,
    private val rootWindowId: Window
) {

    private val keySymCommands = mutableMapOf<Pair<KeySym, Int>, KeyBinding>()

    private val modKeyBindings = mapOf(
        Modifiers.CONTROL to setOf("Ctrl"),
        Modifiers.SHIFT to setOf("Shift"),
        Modifiers.ALT to setOf("Alt"),
        Modifiers.SUPER to setOf("Super", "Win", "Lin"),
        Modifiers.META to setOf("Meta"),
        Modifiers.HYPER to setOf("Hyper"),
    )

    init {
        reloadConfig()
    }

    fun reloadConfig() {
        toggleSessionManager.resetKeyCodes()

        for (keyBinding in keyConfiguration) {
            val (modifierStrings, keyString) = separateKeySymAndModifiers(keyBinding.keys)

            val (mask, modifiers) = getMask(modifierStrings)
            val grabbedMask = if (mask == 0) AnyModifier else mask
            val keySym = getKeySym(keyString)

            keyManager.grabKey(keySym, grabbedMask, rootWindowId)

            keySymCommands[Pair(keySym, mask)] = keyBinding

            if (keyBinding is KeyAction && (keyBinding.action == WmAction.WINDOW_TOGGLE_FWD ||
                        keyBinding.action == WmAction.WINDOW_TOGGLE_BWD)) {
                toggleSessionManager.addModifiers(modifiers, keySym.convert(), mask)
            }
        }
    }

    private fun separateKeySymAndModifiers(keyConfig: String): Pair<List<String>, String> {
        val keyStrings = keyConfig.split('+')
        return Pair(keyStrings.dropLast(1), keyStrings.last())
    }

    private fun getMask(modifierStrings: List<String>): Pair<Int, List<Modifiers>> {
        return modifierStrings.fold(Pair(0, listOf())) { acc, modifierString ->
            val modifier = modKeyBindings.entries
                .single { it.value.contains(modifierString) }
                .key
            Pair(
                acc.first or keyManager.modMasks.getValue(modifier),
                acc.second.plus(modifier)
            )
        }
    }

    private fun getKeySym(keyString: String): KeySym {
        return inputApi.stringToKeysym(keyString)
    }

    /**
     * @return command for a key binding consisting of key sym and key mask. null if there's no command registered for the given key sym+key mask
     */
    fun getBindingForKey(keySym: KeySym, keyMask: Int): KeyBinding? {
        return keySymCommands[Pair(keySym, keyMask)]
    }
}
