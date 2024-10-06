package de.atennert.lcarswm.system

import de.atennert.lcarswm.system.api.WindowUtilApi
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
open class WindowUtilApiMock : WindowUtilApi {
    override val display: CPointer<Display>? = null

    override fun openDisplay(): Boolean {
        TODO("Not yet implemented")
    }

    override fun closeDisplay(): Int {
        TODO("Not yet implemented")
    }

    override fun defaultScreenOfDisplay(): CPointer<Screen>? {
        TODO("Not yet implemented")
    }

    override fun defaultScreenNumber(): Int {
        TODO("Not yet implemented")
    }

    override fun grabServer(): Int {
        TODO("Not yet implemented")
    }

    override fun ungrabServer(): Int {
        TODO("Not yet implemented")
    }

    override fun queryTree(
        window: Window,
        rootReturn: CValuesRef<WindowVar>,
        parentReturn: CValuesRef<WindowVar>,
        childrenReturn: CValuesRef<CPointerVar<WindowVar>>,
        childrenReturnCounts: CValuesRef<UIntVar>
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getWMProtocols(
        window: Window,
        protocolsReturn: CPointer<CPointerVar<AtomVar>>,
        protocolCountReturn: CPointer<IntVar>
    ): Int {
        TODO("Not yet implemented")
    }

    override fun setErrorHandler(handler: XErrorHandler): XErrorHandler? {
        TODO("Not yet implemented")
    }

    override fun internAtom(name: String, onlyIfExists: Boolean): Atom {
        TODO("Not yet implemented")
    }

    override fun changeProperty(
        window: Window,
        propertyAtom: Atom,
        typeAtom: Atom,
        data: UByteArray?,
        format: Int,
        mode: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun deleteProperty(window: Window, propertyAtom: Atom): Int {
        TODO("Not yet implemented")
    }

    override fun getTextProperty(window: Window, textProperty: CPointer<XTextProperty>, propertyAtom: Atom): Int {
        TODO("Not yet implemented")
    }

    override fun xmbTextPropertyToTextList(
        textProperty: CPointer<XTextProperty>,
        resultList: CPointer<CPointerVar<CPointerVar<ByteVar>>>,
        stringCount: CPointer<IntVar>
    ): Int {
        TODO("Not yet implemented")
    }

    override fun localeToUtf8(
        localeString: String,
        stringSize: Long,
        bytesRead: CPointer<ULongVar>?
    ): CPointer<ByteVar>? {
        TODO("Not yet implemented")
    }

    override fun convertLatinToUtf8(
        latinString: String,
        stringSize: Long,
        bytesRead: CPointer<ULongVar>?
    ): CPointer<ByteVar>? {
        TODO("Not yet implemented")
    }

    override fun killClient(window: Window): Int {
        TODO("Not yet implemented")
    }

    override fun createWindow(
        parentWindow: Window,
        measurements: List<Int>,
        depth: Int,
        visual: CPointer<Visual>?,
        attributeMask: ULong,
        attributes: CPointer<XSetWindowAttributes>
    ): Window {
        TODO("Not yet implemented")
    }

    override fun getSelectionOwner(atom: Atom): Window {
        TODO("Not yet implemented")
    }

    override fun setSelectionOwner(atom: Atom, window: Window, time: Time): Int {
        TODO("Not yet implemented")
    }

    override fun getDisplayString(): String {
        TODO("Not yet implemented")
    }

    override fun synchronize(sync: Boolean) {
        TODO("Not yet implemented")
    }

    override fun xFree(xObject: CPointer<*>?) {
        TODO("Not yet implemented")
    }
}