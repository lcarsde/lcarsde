package de.atennert.lcarswm.window

interface ManagedWmWindow<WindowId> : WmWindow<WindowId> {
    /** Frame window ID */
    val frame: WindowId
    /** Name of the program */
    val wmClass: String
    /** Window title (usually depends on program content) */
    val title: String

    fun hasId(windowId: WindowId): Boolean = id == windowId || frame == windowId

    fun isTitleBar(windowId: WindowId): Boolean = false
}