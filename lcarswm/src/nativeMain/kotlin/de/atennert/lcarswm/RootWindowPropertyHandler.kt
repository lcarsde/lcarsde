package de.atennert.lcarswm

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms.*
import de.atennert.lcarswm.conversion.combine
import de.atennert.lcarswm.conversion.toUByteArray
import de.atennert.lcarswm.events.EventBuffer
import de.atennert.lcarswm.events.EventTime
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.system.api.SystemApi
import kotlinx.cinterop.*
import xlib.*

private const val OTHER_WM_SHUTDOWN_TIMEOUT = 15000000

/**
 * Class for handling property (atom) related actions.
 */
@ExperimentalForeignApi
class RootWindowPropertyHandler(
    private val logger: Logger,
    private val system: SystemApi,
    private val rootWindow: Window,
    private val atomLibrary: AtomLibrary,
    private val eventBuffer: EventBuffer
) {
    /** used to set supported properties and track the WM screen ownership */
    val ewmhSupportWindow: Window

    init {
        val windowAttributes = nativeHeap.alloc<XSetWindowAttributes>()
        windowAttributes.override_redirect = X_TRUE
        windowAttributes.event_mask = PropertyChangeMask

        this.ewmhSupportWindow = system.createWindow(
            rootWindow,
            listOf(-100, -100, 1, 1),
            CopyFromParent.convert(),
            CopyFromParent.toCPointer(),
            (CWEventMask or CWOverrideRedirect).convert(),
            windowAttributes.ptr
        )

        system.mapWindow(this.ewmhSupportWindow)
        system.lowerWindow(this.ewmhSupportWindow)

        closeWith(RootWindowPropertyHandler::destroySupportWindow)
    }

    fun becomeScreenOwner(eventTime: EventTime): Boolean {
        val wmSnName = "WM_S${system.defaultScreenNumber()}"
        val wmSn = system.internAtom(wmSnName)

        var currentWmSnOwner = system.getSelectionOwner(wmSn)
        if (currentWmSnOwner == ewmhSupportWindow) {
            currentWmSnOwner = None.convert()
        }
        if (currentWmSnOwner != None.convert<Window>()) {
            system.selectInput(currentWmSnOwner, StructureNotifyMask)
            system.sync(false)
            // TODO check for display error
        }

        val timeStamp = eventTime.lastEventTime
        system.setSelectionOwner(wmSn, ewmhSupportWindow, timeStamp)

        if (system.getSelectionOwner(wmSn) != ewmhSupportWindow) {
            logger.logInfo("Unable to become selection owner on display")
            return false
        }

        if (currentWmSnOwner != None.convert<Window>()) {
            var wait = 0

            while (wait < OTHER_WM_SHUTDOWN_TIMEOUT) {
                if (eventBuffer.findEvent(false) { event ->
                        event.pointed.type == DestroyNotify && event.pointed.xany.window == currentWmSnOwner
                    } != null) {
                    break
                }
                system.usleep((OTHER_WM_SHUTDOWN_TIMEOUT / 10).convert())
                wait += (OTHER_WM_SHUTDOWN_TIMEOUT / 10)
            }

            if (wait >= OTHER_WM_SHUTDOWN_TIMEOUT) {
                logger.logInfo("The active WM is not exiting")
                return false
            }
        }

        sendWmNotification(wmSn, timeStamp)

        return true
    }

    private fun sendWmNotification(wmSn: Atom, timeStamp: Time) {
        val event = nativeHeap.alloc<XEvent>()
        event.xclient.type = ClientMessage
        event.xclient.message_type = atomLibrary[MANAGER]
        event.xclient.window = rootWindow
        event.xclient.format = 32
        event.xclient.data.l[0] = timeStamp.convert()
        event.xclient.data.l[1] = wmSn.convert()
        event.xclient.data.l[2] = this.ewmhSupportWindow.convert()
        event.xclient.data.l[3] = 0
        event.xclient.data.l[4] = 0

        system.sendEvent(rootWindow, false, SubstructureNotifyMask, event.ptr)
    }

    fun setSupportWindowProperties() {
        val ewmhWindowInBytes = ewmhSupportWindow.toUByteArray()

        system.changeProperty(
            rootWindow,
            atomLibrary[NET_SUPPORTING_WM_CHECK],
            atomLibrary[WINDOW],
            ewmhWindowInBytes,
            32
        )
        system.changeProperty(
            ewmhSupportWindow,
            atomLibrary[NET_SUPPORTING_WM_CHECK],
            atomLibrary[WINDOW],
            ewmhWindowInBytes,
            32
        )

        system.changeProperty(
            ewmhSupportWindow,
            atomLibrary[NET_WM_NAME],
            atomLibrary[UTF_STRING],
            "LCARSWM".toUByteArray(),
            8
        )

        system.changeProperty(rootWindow, atomLibrary[NET_SUPPORTED], atomLibrary[ATOM], getSupportedProperties(), 32)
    }

    private fun getSupportedProperties(): UByteArray {
        val supportedProperties = arrayOf(
            NET_SUPPORTING_WM_CHECK,
            NET_WM_NAME,
            NET_WM_DESKTOP,
            NET_WM_STATE,
            NET_CLOSE_WINDOW,
            NET_ACTIVE_WINDOW,
            NET_WM_MOVERESIZE,
            NET_MOVERESIZE_WINDOW,
            NET_RESTACK_WINDOW
        )

        return supportedProperties
            .map { atomLibrary[it] }
            .map { it.toUByteArray() }
            .combine()
    }

    fun unsetWindowProperties() {
        system.deleteProperty(rootWindow, atomLibrary[NET_SUPPORTED])
    }

    fun destroySupportWindow() {
        system.destroyWindow(ewmhSupportWindow)
    }
}
