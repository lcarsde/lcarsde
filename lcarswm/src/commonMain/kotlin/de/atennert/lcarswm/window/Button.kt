package de.atennert.lcarswm.window

interface Button<WindowID> {
    val id: WindowID

    fun changePosition(x: Int, y: Int)

    fun press()

    fun release()
}
