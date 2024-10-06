package de.atennert.lcarswm.window

import de.atennert.lcarswm.ColorSet
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.atom.NumberAtomReader
import de.atennert.lcarswm.atom.TextAtomReader
import de.atennert.lcarswm.drawing.ColorFactory
import de.atennert.lcarswm.drawing.FontProvider
import de.atennert.lcarswm.drawing.IFrameDrawer
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.keys.KeyManager
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.system.*
import kotlinx.cinterop.*
import xlib.*
import kotlin.experimental.ExperimentalNativeApi

@ExperimentalForeignApi
private const val clientEventMask = PropertyChangeMask or StructureNotifyMask or ColormapChangeMask
@ExperimentalForeignApi
private const val clientNoPropagateMask = ButtonPressMask or ButtonReleaseMask or ButtonMotionMask

@ExperimentalForeignApi
@ExperimentalNativeApi
class PosixWindowFactory(
    private val logger: Logger,
    private val display: CPointer<Display>?,
    private val screen: Screen,
    private val colorFactory: ColorFactory,
    private val fontProvider: FontProvider,
    private val keyManager: KeyManager,
    private val monitorManager: MonitorManager<RROutput>,
    private val atomLibrary: AtomLibrary,
    private val eventStore: EventStore,
    private val focusHandler: WindowFocusHandler,
    private val windowList: WindowList,
    private val textAtomReader: TextAtomReader,
    private val numberAtomReader: NumberAtomReader,
    private val frameDrawer: IFrameDrawer,
    private val windowListMessageQueue: MessageQueue,
) : WindowFactory<Window> {

    override fun createButton(
        text: String,
        colorSet: ColorSet,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        onClick: () -> Unit
    ): PosixButton {
        return PosixButton(
            logger, display, screen, colorFactory, fontProvider, monitorManager, keyManager, eventStore,
            text, colorSet, x, y, width, height, onClick
        )
    }

    override fun createWindow(
        id: Window,
        isSetup: Boolean
    ): WmWindow<Window>? {
        wrapXGrabServer(display)
        val windowAttributes = nativeHeap.alloc<XWindowAttributes>()
        if (wrapXGetWindowAttributes(display, id, windowAttributes.ptr) == False) {
            wrapXUngrabServer(display)
            // not a window or something ...
            return null
        }

        if (windowAttributes.override_redirect != False ||
            (isSetup && windowAttributes.map_state != IsViewable)
        ) {
            logger.logInfo("PosixWindowFactory::createWindow::skipping window $id")
            wrapXUngrabServer(display)

            if (!isSetup) {
                wrapXMapWindow(display, id)
            }

            nativeHeap.free(windowAttributes)
            return null
        }

        val attributeSet = nativeHeap.alloc<XSetWindowAttributes>()
        attributeSet.event_mask = clientEventMask
        attributeSet.do_not_propagate_mask = clientNoPropagateMask
        wrapXChangeWindowAttributes(display, id, (CWEventMask or CWDontPropagate).convert(), attributeSet.ptr)

        val window = when {
            PosixAppMenuWindow.isAppMenu(display, atomLibrary, id) -> PosixAppMenuWindow(
                display,
                screen.root,
                monitorManager,
                eventStore,
                atomLibrary,
                focusHandler,
                windowList,
                windowListMessageQueue,
                id,
                windowAttributes.border_width,
            )

            PosixStatusBarWindow.isStatusBar(display, atomLibrary, id) -> PosixStatusBarWindow(
                display,
                screen.root,
                monitorManager,
                eventStore,
                atomLibrary,
                id,
                windowAttributes.border_width,
            )

            else -> {
                val (type, isTransient, transientFor) = determineTransienceAndType(id)
                if (isTransient) {
                    PosixTransientWindow(
                        logger,
                        display,
                        screen,
                        atomLibrary,
                        textAtomReader,
                        keyManager,
                        id,
                        windowAttributes.border_width,
                        type,
                        transientFor,
                    )
                } else {
                    PosixWindow(
                        logger,
                        display,
                        screen,
                        atomLibrary,
                        textAtomReader,
                        frameDrawer,
                        keyManager,
                        id,
                        windowAttributes.border_width,
                        type,
                    )
                }
            }
        }

        nativeHeap.free(windowAttributes)
        return window
    }


    private fun determineTransienceAndType(id: Window): Triple<WindowType, Boolean, Window?> {
        var isTransient = false
        var transientFor: Window? = null
        var type = WindowType.NORMAL

        val transientWindow = nativeHeap.alloc(None.toULong())
        if (wrapXGetTransientForHint(display, id, transientWindow.ptr) != 0) {
            isTransient = true
            if (transientWindow.value != screen.root && transientWindow.value != 0.toULong() && type != WindowType.DOCK) {
                transientFor = transientWindow.value
            }
        }
        nativeHeap.free(transientWindow)

        val windowTypeList = WindowType.values()
            .map { Pair(it, atomLibrary[WINDOW_TYPE_ATOM_MAP.getValue(it)]) }

        val windowTypeProperties =
            numberAtomReader.readULongArrayPropertyOrNull(id, Atoms.NET_WM_WINDOW_TYPE, Atoms.ATOM)
        type = windowTypeProperties?.firstNotNullOfOrNull { propertyAtom ->
            windowTypeList.firstOrNull { it.second == propertyAtom }
        }?.first ?: if (isTransient) WindowType.DIALOG else WindowType.NORMAL

        isTransient = isTransient || TRANSIENT_WINDOW_TYPES.contains(type)

        return Triple(type, isTransient, transientFor)
    }
}