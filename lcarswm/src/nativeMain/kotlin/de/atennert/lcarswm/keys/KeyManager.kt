package de.atennert.lcarswm.keys

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.system.api.InputApi
import kotlinx.cinterop.*
import xlib.*

/**
 * This class handles the registration of keys for keyboard commands.
 */
@ExperimentalForeignApi
class KeyManager(private val inputApi: InputApi) {
    private val modifierIndexes = arrayOf(
        ShiftMask,
        LockMask,
        ControlMask,
        Mod1Mask,
        Mod2Mask,
        Mod3Mask,
        Mod4Mask,
        Mod5Mask
    )

    private var modifierKeymapReference: CPointer<XModifierKeymap>? = null

    private var keymap: CPointer<KeySymVar>? = null

    private val grabbedKeys = mutableMapOf<KeyCode, KeySym>()

    var modMasks = getAllModifierKeys()
        private set

    private var lockMasks = getLockMasks()

    init {
        closeWith(KeyManager::cleanup)
    }

    private fun getAllModifierKeys(): Map<Modifiers, Int> {
        modifierKeymapReference = inputApi.getModifierMapping()
        val modifierKeymap = modifierKeymapReference?.pointed ?: return emptyMap()
        val (minKeyCodes, maxKeyCodes) = inputApi.getDisplayKeyCodeMinMaxCounts()

        val keySymsPerKeyCode = IntArray(1)
        keySymsPerKeyCode.usePinned { keySymsPerKeyCodePinned ->
            keymap = inputApi.getKeyboardMapping(
                minKeyCodes.convert(),
                (maxKeyCodes - minKeyCodes + 1).convert(),
                keySymsPerKeyCodePinned.addressOf(0)
            )!!
        }

        var superLUsed = false
        var hyperLUsed = false
        var altLUsed = false
        var metaLUsed = false

        val modifierMasks = mutableMapOf(
            Modifiers.CAPS_LOCK to LockMask,
            Modifiers.SHIFT to ShiftMask,
            Modifiers.CONTROL to ControlMask,
        )

        for (i in modifierIndexes.indices) {
            val mask = 1.shl(i)
            for (j in 0 until modifierKeymap.max_keypermod) {
                val keyCode = modifierKeymap.modifiermap!![i * modifierKeymap.max_keypermod + j].convert<Int>()
                if (keyCode != 0) {
                    for (k in 0 until keySymsPerKeyCode[0]) {
                        val keySym = keymap!![(keyCode - minKeyCodes) * keySymsPerKeyCode[0] + k]
                        if (keySym.convert<Long>() != NoSymbol) {
                            when (keySym.convert<Int>()) {
                                XK_Num_Lock -> {
                                    modifierMasks[Modifiers.NUM_LOCK] =
                                        modifierMasks.getOrElse(Modifiers.NUM_LOCK, { 0 }).or(mask)
                                }
                                XK_Scroll_Lock -> {
                                    modifierMasks[Modifiers.SCROLL_LOCK] =
                                        modifierMasks.getOrElse(Modifiers.SCROLL_LOCK, { 0 }).or(mask)
                                }

                                XK_Super_L -> {
                                    modifierMasks[Modifiers.SUPER] = if (superLUsed) {
                                        modifierMasks.getOrElse(Modifiers.SUPER, { 0 }).or(mask)
                                    } else {
                                        superLUsed = true
                                        mask // overwrite any super-r stuff
                                    }
                                }
                                XK_Super_R -> {
                                    if (!superLUsed) {
                                        modifierMasks[Modifiers.SUPER] = modifierMasks.getOrElse(
                                            Modifiers.SUPER, { 0 }).or(mask)
                                    }
                                }

                                XK_Hyper_L -> modifierMasks[Modifiers.HYPER] = if (hyperLUsed) {
                                    modifierMasks.getOrElse(Modifiers.HYPER, { 0 }).or(mask)
                                } else {
                                    hyperLUsed = true
                                    mask // overwrite any hyper-r stuff
                                }
                                XK_Hyper_R -> if (!hyperLUsed) {
                                    modifierMasks[Modifiers.HYPER] = modifierMasks.getOrElse(
                                        Modifiers.HYPER, { 0 }).or(mask)
                                }

                                XK_Alt_L -> modifierMasks[Modifiers.ALT] = if (altLUsed) {
                                    modifierMasks.getOrElse(Modifiers.ALT, { 0 }).or(mask)
                                } else {
                                    altLUsed = true
                                    mask // overwrite any alt-r stuff
                                }
                                XK_Alt_R -> if (!altLUsed) {
                                    modifierMasks[Modifiers.ALT] = modifierMasks.getOrElse(
                                        Modifiers.ALT, { 0 }).or(mask)
                                }

                                XK_Meta_L -> modifierMasks[Modifiers.META] = if (metaLUsed) {
                                    modifierMasks.getOrElse(Modifiers.META, { 0 }).or(mask)
                                } else {
                                    metaLUsed = true
                                    mask // overwrite any meta-r stuff
                                }
                                XK_Meta_R -> if (!metaLUsed) {
                                    modifierMasks[Modifiers.META] = modifierMasks.getOrElse(
                                        Modifiers.META, { 0 }).or(mask)
                                }
                            }
                        }
                    }
                }
            }
        }

        return modifierMasks
    }

    private fun getLockMasks(): List<Int> {
        val numMask = modMasks[Modifiers.NUM_LOCK] ?: 0
        val capsMask = modMasks[Modifiers.CAPS_LOCK] ?: 0
        val scrollMask = modMasks[Modifiers.SCROLL_LOCK] ?: 0

        return listOf(
            0,
            numMask,
            capsMask,
            scrollMask,
            numMask or capsMask,
            numMask or scrollMask,
            capsMask or scrollMask,
            numMask or capsMask or scrollMask
        )
    }

    /**
     * Ungrab all grabbed keys
     */
    fun ungrabAllKeys(rootWindowId: Window) {
        inputApi.ungrabKey(rootWindowId)
        grabbedKeys.clear()
    }

    /**
     * Reload the modifier mask configuration.
     */
    fun reloadConfig() {
        cleanup()
        modMasks = getAllModifierKeys()
        lockMasks = getLockMasks()
    }

    /**
     * Grab a key binding consisting of a key sym and a key modifier
     */
    fun grabKey(keySym: KeySym, modifiers: Int, rootWindowId: Window) {
        val keyCode = inputApi.keysymToKeycode(keySym)

        if (keyCode.convert<Int>() != 0) {
            grabbedKeys[keyCode] = keySym
            lockMasks.forEach { lockMask ->
                inputApi.grabKey(keyCode.convert(), (modifiers or lockMask).convert(), rootWindowId, GrabModeAsync)
            }
        }
    }

    fun grabButton(button: UInt, modifiers: Int, windowId: Window, mask: UInt, pointerMode: Int, cursor: Cursor) {
        lockMasks.forEach { lockMask ->
            inputApi.grabButton(
                button,
                (modifiers or lockMask).convert(),
                windowId,
                true,
                mask,
                pointerMode,
                GrabModeAsync,
                None.convert(),
                cursor
            )
        }
    }

    fun ungrabButton(button: UInt, modifiers: Int, windowId: Window) {
        lockMasks.forEach { lockMask ->
            inputApi.ungrabButton(button, (modifiers or lockMask).convert(), windowId)
        }
    }

    /**
     * @return key sym for a given key code
     */
    fun getKeySym(keyCode: KeyCode): KeySym? = grabbedKeys[keyCode]

    /**
     * Cleanup acquired X data
     */
    private fun cleanup() {
        if (modifierKeymapReference != null) {
            inputApi.freeModifiermap(modifierKeymapReference)
            modifierKeymapReference = null
        }

        if (keymap != null) {
            inputApi.xFree(keymap)
            keymap = null
        }
    }

    /**
     * @return filtered given modifier mask from CAPS / NUM and SCROLL lock
     */
    fun filterMask(keyMask: UInt): Int {
        var filteredMask = keyMask.convert<Int>() and 0xFF
        filteredMask = filteredMask and modMasks.getOrElse(Modifiers.CAPS_LOCK, { 0 }).inv()
        filteredMask = filteredMask and modMasks.getOrElse(Modifiers.NUM_LOCK, { 0 }).inv()
        filteredMask = filteredMask and modMasks.getOrElse(Modifiers.SCROLL_LOCK, { 0 }).inv()
        return filteredMask
    }
}
