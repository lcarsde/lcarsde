package de.atennert.lcarswm.system

import de.atennert.lcarswm.X_FALSE
import de.atennert.lcarswm.X_TRUE
import de.atennert.lcarswm.signal.Signal
import de.atennert.lcarswm.system.api.SystemApi
import de.atennert.lcarswm.toX
import gtk.*
import kotlinx.cinterop.*
import platform.posix.sigaction
import platform.posix.sigemptyset
import platform.posix.sigfillset
import platform.posix.sigset_t
import xlib.*

/**
 * This is the facade for accessing system functions.
 */
@ExperimentalForeignApi
class SystemFacade : SystemApi {
    override var display: CPointer<Display>? = null
        private set

    override fun openDisplay(): Boolean {
        this.display = XOpenDisplay(null)
        return this.display != null
    }

    override fun closeDisplay(): Int {
        return XCloseDisplay(display)
    }

    override fun defaultScreenOfDisplay(): CPointer<Screen>? {
        return XDefaultScreenOfDisplay(display)
    }

    override fun defaultScreenNumber(): Int {
        return XDefaultScreen(display)
    }

    override fun grabServer(): Int {
        return XGrabServer(display)
    }

    override fun ungrabServer(): Int {
        return XUngrabServer(display)
    }

    override fun queryTree(
        window: Window,
        rootReturn: CValuesRef<WindowVar>,
        parentReturn: CValuesRef<WindowVar>,
        childrenReturn: CValuesRef<CPointerVar<WindowVar>>,
        childrenReturnCounts: CValuesRef<UIntVar>
    ): Int {
        return XQueryTree(display, window, rootReturn, parentReturn, childrenReturn, childrenReturnCounts)
    }

    override fun getWMProtocols(
        window: Window,
        protocolsReturn: CPointer<CPointerVar<AtomVar>>,
        protocolCountReturn: CPointer<IntVar>
    ): Int {
        return XGetWMProtocols(display, window, protocolsReturn, protocolCountReturn)
    }

    override fun setErrorHandler(handler: XErrorHandler): XErrorHandler? {
        return XSetErrorHandler(handler)
    }

    override fun internAtom(name: String, onlyIfExists: Boolean): Atom {
        return XInternAtom(display, name, onlyIfExists.toX())
    }

    override fun changeProperty(
        window: Window,
        propertyAtom: Atom,
        typeAtom: Atom,
        data: UByteArray?,
        format: Int,
        mode: Int
    ): Int {
        val bytesPerData = format.div(8)
        val dataCount = data?.size?.div(bytesPerData) ?: 0
        return XChangeProperty(display, window, propertyAtom, typeAtom, format, mode, data?.toCValues(), dataCount)
    }

    override fun deleteProperty(window: Window, propertyAtom: Atom): Int {
        return XDeleteProperty(display, window, propertyAtom)
    }

    override fun getTextProperty(window: Window, textProperty: CPointer<XTextProperty>, propertyAtom: Atom): Int {
        return XGetTextProperty(display, window, textProperty, propertyAtom)
    }

    override fun xmbTextPropertyToTextList(
        textProperty: CPointer<XTextProperty>,
        resultList: CPointer<CPointerVar<CPointerVar<ByteVar>>>,
        stringCount: CPointer<IntVar>
    ): Int {
        return XmbTextPropertyToTextList(display, textProperty, resultList, stringCount)
    }

    override fun localeToUtf8(
        localeString: String,
        stringSize: Long,
        bytesRead: CPointer<ULongVar>?
    ): CPointer<ByteVar>? {
        return g_locale_to_utf8(localeString, stringSize, bytesRead, null, null)
    }

    override fun convertLatinToUtf8(
        latinString: String,
        stringSize: Long,
        bytesRead: CPointer<ULongVar>?
    ): CPointer<ByteVar>? {
        return g_convert(
            latinString, stringSize, "utf-8", "iso-8859-1",
            bytesRead, null, null
        )
    }

    override fun killClient(window: Window): Int {
        return XKillClient(display, window)
    }

    override fun createWindow(
        parentWindow: Window,
        measurements: List<Int>,
        depth: Int,
        visual: CPointer<Visual>?,
        attributeMask: ULong,
        attributes: CPointer<XSetWindowAttributes>
    ): Window {
        return XCreateWindow(
            display,
            parentWindow,
            measurements[0],
            measurements[1],
            measurements[2].convert(),
            measurements[3].convert(),
            0.convert(),
            depth,
            InputOutput.convert(),
            visual,
            attributeMask,
            attributes
        )
    }

    override fun getSelectionOwner(atom: Atom): Window {
        return XGetSelectionOwner(display, atom)
    }

    override fun setSelectionOwner(atom: Atom, window: Window, time: Time): Int {
        return XSetSelectionOwner(display, atom, window, time)
    }

    override fun getDisplayString(): String {
        return XDisplayString(this.display)?.toKString() ?: ""
    }

    override fun synchronize(sync: Boolean) {
        XSynchronize(display, sync.toX())
    }

    override fun xFree(xObject: CPointer<*>?) {
        XFree(xObject)
    }

    override fun sync(discardQueuedEvents: Boolean): Int {
        return XSync(display, discardQueuedEvents.toX())
    }

    override fun sendEvent(
        window: Window,
        propagate: Boolean,
        eventMask: Long,
        event: CPointer<XEvent>
    ): Int {
        event.pointed.xconfigure.display = display
        event.pointed.xclient.display = display

        return XSendEvent(display, window, propagate.toX(), eventMask, event)
    }

    override fun nextEvent(event: CPointer<XEvent>): Int {
        return XNextEvent(display, event)
    }

    override fun getQueuedEvents(mode: Int): Int {
        return XEventsQueued(display, mode)
    }

    override fun lowerWindow(window: Window): Int {
        return XLowerWindow(display, window)
    }

    override fun mapWindow(window: Window): Int {
        return XMapWindow(display, window)
    }

    override fun destroyWindow(window: Window): Int {
        return XDestroyWindow(display, window)
    }

    override fun usleep(time: UInt) {
        platform.posix.usleep(time)
    }

    override fun abort() {
        platform.posix.abort()
    }

    override fun sigFillSet(sigset: CPointer<sigset_t>) {
        sigfillset(sigset)
    }

    override fun sigEmptySet(sigset: CPointer<sigset_t>) {
        sigemptyset(sigset)
    }

    override fun sigAction(signal: Signal, newSigAction: CPointer<sigaction>, oldSigAction: CPointer<sigaction>?) {
        sigaction(signal.signalValue, newSigAction, oldSigAction)
    }

    override fun selectInput(window: Window, mask: Long): Int {
        return XSelectInput(display, window, mask)
    }

    override fun setInputFocus(window: Window, revertTo: Int, time: Time): Int {
        return XSetInputFocus(display, window, revertTo, time)
    }

    override fun grabKey(
        keyCode: Int,
        modifiers: UInt,
        window: Window,
        keyboardMode: Int
    ): Int {
        return XGrabKey(display, keyCode, modifiers, window, X_TRUE, GrabModeAsync, keyboardMode)
    }

    override fun ungrabKey(window: Window) {
        XUngrabKey(display, AnyKey.convert(), AnyModifier.convert(), window)
    }

    override fun grabKeyboard(window: Window, time: Time) {
        XGrabKeyboard(display, window, X_FALSE, GrabModeAsync, GrabModeAsync, time)
    }

    override fun ungrabKeyboard(time: Time) {
        XUngrabKeyboard(display, time)
    }

    override fun grabButton(
        button: UInt,
        modifiers: UInt,
        window: Window,
        ownerEvents: Boolean,
        mask: UInt,
        pointerMode: Int,
        keyboardMode: Int,
        windowToConfineTo: Window,
        cursor: Cursor
    ): Int {
        return XGrabButton(
            display,
            button,
            modifiers,
            window,
            ownerEvents.toX(),
            mask,
            pointerMode,
            keyboardMode,
            windowToConfineTo,
            cursor
        )
    }

    override fun ungrabButton(button: UInt, modifiers: UInt, window: Window): Int {
        return XUngrabButton(display, button, modifiers, window)
    }

    override fun createFontCursor(fontValue: Int): Cursor {
        return XCreateFontCursor(display, fontValue.convert())
    }

    override fun defineCursor(window: Window, cursor: Cursor): Int {
        return XDefineCursor(display, window, cursor)
    }

    override fun allowEvents(eventMode: Int, time: Time) {
        XAllowEvents(display, eventMode, time)
    }

    override fun readXmlFile(filePath: String): xmlDocPtr? {
        return xmlReadFile(filePath, null, 0)
    }

    override fun getXmlRootElement(xmlDoc: xmlDocPtr): xmlNodePtr? {
        return xmlDocGetRootElement(xmlDoc)
    }

    override fun freeXmlDoc(xmlDoc: xmlDocPtr) {
        xmlFreeDoc(xmlDoc)
    }

    override fun getModifierMapping(): CPointer<XModifierKeymap>? {
        return XGetModifierMapping(display)
    }

    override fun getDisplayKeyCodeMinMaxCounts(): Pair<Int, Int> {
        val keyCodes = IntArray(2)
        keyCodes.usePinned {
            XDisplayKeycodes(display, it.addressOf(0), it.addressOf(1))
        }
        return Pair(keyCodes[0], keyCodes[1])
    }

    override fun getKeyboardMapping(
        firstKeyCode: KeyCode,
        keyCodeCount: Int,
        keySymsPerKeyCode: CPointer<IntVar>
    ): CPointer<KeySymVar>? {
        return XGetKeyboardMapping(display, firstKeyCode, keyCodeCount, keySymsPerKeyCode)
    }

    override fun keysymToKeycode(keySym: KeySym): KeyCode {
        return XKeysymToKeycode(display, keySym)
    }

    override fun stringToKeysym(s: String): KeySym {
        return XStringToKeysym(s)
    }

    override fun freeModifiermap(modifierMap: CPointer<XModifierKeymap>?) {
        XFreeModifiermap(modifierMap)
    }

    override fun rQueryExtension(eventBase: CPointer<IntVar>, errorBase: CPointer<IntVar>): Int {
        return XRRQueryExtension(display, eventBase, errorBase)
    }

    override fun rSelectInput(window: Window, mask: Int) {
        XRRSelectInput(display, window, mask)
    }

    override fun rGetScreenResources(window: Window): CPointer<XRRScreenResources>? {
        return XRRGetScreenResources(display, window)
    }

    override fun rGetOutputPrimary(window: Window): RROutput {
        return XRRGetOutputPrimary(display, window)
    }

    override fun rGetOutputInfo(
        resources: CPointer<XRRScreenResources>,
        output: RROutput
    ): CPointer<XRROutputInfo>? {
        return XRRGetOutputInfo(display, resources, output)
    }

    override fun rGetCrtcInfo(
        resources: CPointer<XRRScreenResources>,
        crtc: RRCrtc
    ): CPointer<XRRCrtcInfo>? {
        return XRRGetCrtcInfo(display, resources, crtc)
    }
}