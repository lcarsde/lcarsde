package de.atennert.lcarswm.system

import de.atennert.lcarswm.X_FALSE
import de.atennert.lcarswm.X_TRUE
import de.atennert.lcarswm.signal.Signal
import de.atennert.lcarswm.system.api.SystemApi
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
        return XInternAtom(display, name, convertToXBoolean(onlyIfExists))
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
        return g_convert(latinString, stringSize, "utf-8", "iso-8859-1",
            bytesRead, null, null)
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
        return XCreateWindow(display, parentWindow, measurements[0], measurements[1], measurements[2].convert(), measurements[3].convert(), 0.convert(), depth, InputOutput.convert(), visual, attributeMask, attributes)
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
        XSynchronize(display, convertToXBoolean(sync))
    }

    override fun xFree(xObject: CPointer<*>?) {
        XFree(xObject)
    }

    override fun sync(discardQueuedEvents: Boolean): Int {
        return XSync(display, convertToXBoolean(discardQueuedEvents))
    }

    override fun sendEvent(
        window: Window,
        propagate: Boolean,
        eventMask: Long,
        event: CPointer<XEvent>
    ): Int {
        event.pointed.xconfigure.display = display
        event.pointed.xclient.display = display

        return XSendEvent(display, window, convertToXBoolean(propagate), eventMask, event)
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
        return XGrabButton(display, button, modifiers, window, convertToXBoolean(ownerEvents), mask, pointerMode, keyboardMode, windowToConfineTo, cursor)
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

    override fun fillRectangle(
        drawable: Drawable,
        graphicsContext: GC,
        x: Int,
        y: Int,
        width: UInt,
        height: UInt
    ): Int {
        return XFillRectangle(display, drawable, graphicsContext, x, y, width, height)
    }

    override fun fillRectangles(
        drawable: Drawable,
        graphicsContext: GC,
        rects: CValuesRef<XRectangle>,
        rectCount: Int
    ): Int {
        return XFillRectangles(display, drawable, graphicsContext, rects, rectCount)
    }

    override fun drawPoint(drawable: Drawable, graphicsContext: GC, x: Int, y: Int): Int {
        return XDrawPoint(display, drawable, graphicsContext, x, y)
    }

    override fun putImage(
        drawable: Drawable,
        graphicsContext: GC,
        image: CValuesRef<XImage>,
        x: Int,
        y: Int,
        width: UInt,
        height: UInt
    ): Int {
        return XPutImage(display, drawable, graphicsContext, image, 0, 0, x, y, width, height)
    }

    override fun createGC(
        drawable: Drawable,
        mask: ULong,
        gcValues: CValuesRef<XGCValues>?
    ): GC? {
        return XCreateGC(display, drawable, mask, gcValues)
    }

    override fun freeGC(graphicsContext: GC): Int {
        return XFreeGC(display, graphicsContext)
    }

    override fun createColormap(
        window: Window,
        visual: CValuesRef<Visual>,
        alloc: Int
    ): Colormap {
        return XCreateColormap(display, window, visual, alloc)
    }

    override fun allocColor(colorMap: Colormap, color: CPointer<XColor>): Int {
        return XAllocColor(display, colorMap, color)
    }

    override fun freeColors(
        colorMap: Colormap,
        pixels: CValuesRef<ULongVar>,
        pixelCount: Int
    ): Int {
        return XFreeColors(display, colorMap, pixels, pixelCount, 0.convert())
    }

    override fun freeColormap(colorMap: Colormap): Int {
        return XFreeColormap(display, colorMap)
    }

    override fun readXpmFileToImage(
        imagePath: String,
        imageBuffer: CPointer<CPointerVar<XImage>>
    ): Int {
        return XpmReadFileToImage(display, imagePath, imageBuffer, null, null)
    }

    override fun createPixmap(drawable: Drawable, width: UInt, height: UInt, depth: UInt): Pixmap {
        return XCreatePixmap(display, drawable, width, height, depth)
    }

    override fun xftDrawCreate(drawable: Drawable, visual: CValuesRef<Visual>, colorMap: Colormap): CPointer<XftDraw>? {
        return XftDrawCreate(display, drawable, visual, colorMap)
    }

    override fun xftDrawRect(xftDraw: CPointer<XftDraw>?, color: CPointer<XftColor>, x: Int, y: Int, width: UInt, height: UInt) {
        XftDrawRect(xftDraw, color, x, y, width, height)
    }

    override fun setWindowBackgroundPixmap(window: Window, pixmap: Pixmap) {
        XSetWindowBackgroundPixmap(display, window, pixmap)
    }

    override fun clearWindow(window: Window) {
        XClearWindow(display, window)
    }

    override fun freePixmap(pixmap: Pixmap) {
        XFreePixmap(display, pixmap)
    }

    override fun xftGetContext(screen: Int): CPointer<PangoContext>? {
        return pango_xft_get_context(display, screen)
    }

    override fun newLayout(pango: CPointer<PangoContext>?): CPointer<PangoLayout>? {
        return pango_layout_new(pango)
    }

    override fun getFontDescription(): CPointer<PangoFontDescription>? {
        return pango_font_description_new()
    }

    override fun getDefaultLanguage(): CPointer<PangoLanguage>? {
        return pango_language_get_default()
    }

    override fun setFontDescriptionFamily(font: CPointer<PangoFontDescription>?, family: String) {
        pango_font_description_set_family(font, family)
    }

    override fun setFontDescriptionWeight(font: CPointer<PangoFontDescription>?, weight: PangoWeight) {
        pango_font_description_set_weight(font, weight)
    }

    override fun setFontDescriptionStyle(font: CPointer<PangoFontDescription>?, style: PangoStyle) {
        pango_font_description_set_style(font, style)
    }

    override fun setFontDescriptionSize(font: CPointer<PangoFontDescription>?, size: Int) {
        pango_font_description_set_size(font, size)
    }

    override fun freeFontDescription(font: CPointer<PangoFontDescription>?) {
        pango_font_description_free(font)
    }

    override fun setLayoutFontDescription(
        layout: CPointer<PangoLayout>?,
        fontDescription: CPointer<PangoFontDescription>?
    ) {
        pango_layout_set_font_description(layout, fontDescription)
    }

    override fun setLayoutWrapMode(layout: CPointer<PangoLayout>?, wrapMode: PangoWrapMode) {
        pango_layout_set_wrap(layout, wrapMode)
    }

    override fun setLayoutText(layout: CPointer<PangoLayout>?, text: String) {
        pango_layout_set_text(layout, text, text.length)
    }

    override fun setLayoutWidth(layout: CPointer<PangoLayout>?, width: Int) {
        pango_layout_set_width(layout, width)
    }

    override fun setLayoutEllipsizeMode(layout: CPointer<PangoLayout>?, ellipsizeMode: PangoEllipsizeMode) {
        pango_layout_set_ellipsize(layout, ellipsizeMode)
    }

    override fun setLayoutSingleParagraphMode(layout: CPointer<PangoLayout>?, setting: Boolean) {
        pango_layout_set_single_paragraph_mode(layout, convertToXBoolean(setting))
    }

    override fun getLayoutPixelExtents(layout: CPointer<PangoLayout>?, logicalRectangle: CPointer<PangoRectangle>?) {
        pango_layout_get_pixel_extents(layout, null, logicalRectangle)
    }

    override fun getLayoutLineReadonly(layout: CPointer<PangoLayout>?, line: Int): CPointer<PangoLayoutLine>? {
        return pango_layout_get_line_readonly(layout, line)
    }

    override fun xftRenderLayoutLine(
        draw: CPointer<XftDraw>?,
        color: CPointer<XftColor>,
        line: CPointer<PangoLayoutLine>?,
        x: Int,
        y: Int
    ) {
        pango_xft_render_layout_line(draw, color, line, x, y)
    }

    override fun getFontMetrics(
        context: CPointer<PangoContext>?,
        font: CPointer<PangoFontDescription>?,
        language: CPointer<PangoLanguage>?
    ): CPointer<PangoFontMetrics>? {
        return pango_context_get_metrics(context, font, language)
    }

    override fun getFontAscentDescent(metrics: CPointer<PangoFontMetrics>?): Pair<Int, Int> {
        val ascent = pango_font_metrics_get_ascent(metrics)
        val descent = pango_font_metrics_get_descent(metrics)
        return Pair(ascent, descent)
    }

    override fun freeFontMetrics(metrics: CPointer<PangoFontMetrics>?) {
        pango_font_metrics_unref(metrics)
    }

    private fun convertToXBoolean(ownerEvents: Boolean): Int = when (ownerEvents) {
        true  -> X_TRUE
        false -> X_FALSE
    }
}