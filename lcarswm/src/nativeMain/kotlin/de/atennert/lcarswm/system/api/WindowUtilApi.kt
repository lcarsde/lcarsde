package de.atennert.lcarswm.system.api

import kotlinx.cinterop.*
import xlib.*

/**
 *
 */
@ExperimentalForeignApi
interface WindowUtilApi {
    val display: CPointer<Display>?

    /** @return true on success, false otherwise */
    fun openDisplay(): Boolean

    fun closeDisplay(): Int

    fun defaultScreenOfDisplay(): CPointer<Screen>?

    fun defaultScreenNumber(): Int

    fun grabServer(): Int

    fun ungrabServer(): Int

    fun queryTree(window: Window, rootReturn: CValuesRef<WindowVar>, parentReturn: CValuesRef<WindowVar>, childrenReturn: CValuesRef<CPointerVar<WindowVar>>, childrenReturnCounts: CValuesRef<UIntVar>): Int

    fun getWMProtocols(window: Window, protocolsReturn: CPointer<CPointerVar<AtomVar>>, protocolCountReturn: CPointer<IntVar>): Int

    fun setErrorHandler(handler: XErrorHandler): XErrorHandler?

    fun internAtom(name: String, onlyIfExists: Boolean = false): Atom

    fun changeProperty(window: Window, propertyAtom: Atom, typeAtom: Atom, data: UByteArray?, format: Int, mode: Int = PropModeReplace): Int

    fun deleteProperty(window: Window, propertyAtom: Atom): Int

    fun getTextProperty(window: Window, textProperty: CPointer<XTextProperty>, propertyAtom: Atom): Int

    fun xmbTextPropertyToTextList(
        textProperty: CPointer<XTextProperty>,
        resultList: CPointer<CPointerVar<CPointerVar<ByteVar>>>,
        stringCount: CPointer<IntVar>
    ): Int

    fun localeToUtf8(localeString: String, stringSize: Long, bytesRead: CPointer<ULongVar>?): CPointer<ByteVar>?

    fun convertLatinToUtf8(latinString: String, stringSize: Long, bytesRead: CPointer<ULongVar>?): CPointer<ByteVar>?

    fun killClient(window: Window): Int

    fun createWindow(parentWindow: Window, measurements: List<Int>, depth: Int, visual: CPointer<Visual>?, attributeMask: ULong, attributes: CPointer<XSetWindowAttributes>): Window

    fun getSelectionOwner(atom: Atom): Window

    fun setSelectionOwner(atom: Atom, window: Window, time: Time): Int

    fun getDisplayString(): String

    fun synchronize(sync: Boolean)

    fun xFree(xObject: CPointer<*>?)
}