package de.atennert.lcarswm.system.api

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import xlib.*

/**
 *
 */
@ExperimentalForeignApi
interface InputApi {
    fun selectInput(window: Window, mask: Long): Int

    fun setInputFocus(window: Window, revertTo: Int, time: Time): Int

    fun grabKey(keyCode: Int, modifiers: UInt, window: Window, keyboardMode: Int): Int

    fun ungrabKey(window: Window)

    fun grabKeyboard(window: Window, time: Time)

    fun ungrabKeyboard(time: Time)

    fun grabButton(button: UInt, modifiers: UInt, window: Window, ownerEvents: Boolean,
                   mask: UInt, pointerMode: Int, keyboardMode: Int, windowToConfineTo: Window, cursor: Cursor): Int

    fun ungrabButton(button: UInt, modifiers: UInt, window: Window): Int

    fun createFontCursor(fontValue: Int): Cursor

    fun defineCursor(window: Window, cursor: Cursor): Int

    fun getModifierMapping(): CPointer<XModifierKeymap>?

    fun getDisplayKeyCodeMinMaxCounts(): Pair<Int, Int>

    fun getKeyboardMapping(firstKeyCode: KeyCode, keyCodeCount: Int, keySymsPerKeyCode: CPointer<IntVar>): CPointer<KeySymVar>?

    fun keysymToKeycode(keySym: KeySym): KeyCode

    fun stringToKeysym(s: String): KeySym

    fun freeModifiermap(modifierMap: CPointer<XModifierKeymap>?)

    fun xFree(xObject: CPointer<*>?)

    fun allowEvents(eventMode: Int, time: Time)

    fun readXmlFile(filePath: String): xmlDocPtr?

    fun getXmlRootElement(xmlDoc: xmlDocPtr): xmlNodePtr?

    fun freeXmlDoc(xmlDoc: xmlDocPtr)
}