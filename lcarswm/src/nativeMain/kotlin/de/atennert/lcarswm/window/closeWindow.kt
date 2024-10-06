package de.atennert.lcarswm.window

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.system.api.SystemApi
import kotlinx.cinterop.*
import xlib.AtomVar
import xlib.ClientMessage
import xlib.Window
import xlib.XEvent

@ExperimentalForeignApi
fun closeWindow(logger: Logger, systemApi: SystemApi, atomLibrary: AtomLibrary, windowId: Window) {
    logger.logInfo("::closeActiveWindow::focused window: $windowId")

    val supportedProtocols = nativeHeap.allocPointerTo<AtomVar>()
    val numSupportedProtocols = IntArray(1)

    val protocolsResult = numSupportedProtocols.usePinned { numSupportedProtocolsPinned ->
        systemApi.getWMProtocols(windowId, supportedProtocols.ptr, numSupportedProtocolsPinned.addressOf(0))
    }

    if (protocolsResult == 0) {
        logger.logDebug("::closeActiveWindow::kill window due to erroneous protocols")
        systemApi.killClient(windowId)
        nativeHeap.free(supportedProtocols)
        return
    }

    val protocols = ULongArray(numSupportedProtocols[0]) { supportedProtocols.value!![it] }
    nativeHeap.free(supportedProtocols)

    if (!protocols.contains(atomLibrary[Atoms.WM_DELETE_WINDOW])) {
        logger.logDebug("::closeActiveWindow::kill window due to missing WM_DELETE_WINDOW")
        systemApi.killClient(windowId)
    } else {
        logger.logDebug("::closeActiveWindow::gracefully send WM_DELETE_WINDOW request")
        val msg = nativeHeap.alloc<XEvent>()
        msg.xclient.type = ClientMessage
        msg.xclient.message_type = atomLibrary[Atoms.WM_PROTOCOLS]
        msg.xclient.window = windowId
        msg.xclient.format = 32
        msg.xclient.data.l[0] = atomLibrary[Atoms.WM_DELETE_WINDOW].convert()
        systemApi.sendEvent(windowId, false, 0, msg.ptr)
    }
}