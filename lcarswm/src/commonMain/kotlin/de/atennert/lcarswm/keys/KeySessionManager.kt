package de.atennert.lcarswm.keys

import de.atennert.lcarswm.Environment
import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert

@ExperimentalForeignApi
class KeySessionManager(private val logger: Logger, private val env: Environment) {
    private val listeners = mutableListOf<KeySessionListener>()

    private var keyCodes = mutableSetOf<UInt>()
    private var keyMaskCodes = mutableListOf<List<UInt>>()
    private var keyMasks = mutableListOf<Int>()

    fun resetKeyCodes() {
        keyCodes.clear()
        keyMaskCodes.clear()
        keyMasks.clear()
    }

    fun addModifiers(modifiers: List<Modifiers>, keySym: Int, keyMask: Int) {
        val keySyms = mutableListOf<Int>()

        for (modifier in modifiers) {
            when (modifier) {
                Modifiers.SHIFT -> {
                    keySyms.add(keyShiftL)
                    keySyms.add(keyShiftR)
                }
                Modifiers.CONTROL -> {
                    keySyms.add(keyControlL)
                    keySyms.add(keyControlR)
                }
                Modifiers.SUPER -> {
                    keySyms.add(keySuperL)
                    keySyms.add(keySuperR)
                }
                Modifiers.HYPER -> {
                    keySyms.add(keyHyperL)
                    keySyms.add(keyHyperR)
                }
                Modifiers.META -> {
                    keySyms.add(keyMetaL)
                    keySyms.add(keyMetaR)
                }
                Modifiers.ALT -> {
                    keySyms.add(keyAltL)
                    keySyms.add(keyAltR)
                }
                else -> throw IllegalArgumentException("Unsupported modifier: $modifier")
            }
        }

        keyCodes.add(keysymToKeycode(env, keySym))
        keyMasks.add(keyMask)
        keyMaskCodes.add(keySyms.map { keysymToKeycode(env, it) })
    }

    fun addListener(listener: KeySessionListener) {
        listeners.add(listener)
    }

    fun pressKeys(keyCode: UInt, keyMask: Int) {
        val isSameSession = keyMasks.contains(keyMask) and keyCodes.contains(keyCode)
        if (!isSameSession) {
            logger.logDebug("KeySessionManager::pressKeys::stop session")
            listeners.forEach(KeySessionListener::stopSession)
        }
    }

    fun releaseKeys(keyCode: UInt) {
        val breaksAllMasks = keyMaskCodes.all { it.contains(keyCode.convert()) }
        if (breaksAllMasks) {
            logger.logDebug("KeySessionManager::releaseKeys::stop session")
            listeners.forEach(KeySessionListener::stopSession)
        }
    }

    interface KeySessionListener {
        fun stopSession()
    }
}
