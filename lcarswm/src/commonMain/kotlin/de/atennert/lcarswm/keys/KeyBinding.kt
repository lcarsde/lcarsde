package de.atennert.lcarswm.keys

/** base class for key bindings */
sealed class KeyBinding {
    abstract val keys: String
    abstract val command: String
}

/** The key execution defines a key binding for executing a command line command with exec */
data class KeyExecution(override val keys: String, override val command: String) : KeyBinding()

/** The key action defines a key binding for a window manager action */
data class KeyAction(override val keys: String, val action: WmAction) : KeyBinding() {
    override val command: String
        get() = action.keys.first()
}
