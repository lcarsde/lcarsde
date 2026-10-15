package de.atennert.lcarswm.system

import gtk.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.linux.mq_close
import platform.linux.mq_receive
import platform.linux.mq_send
import platform.linux.mq_unlink
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@ExperimentalForeignApi
open class SystemCallMocker {
    @BeforeTest
    open fun setup() {
        wrapXGrabServer = ::mockXGrabServer
        wrapXUngrabServer = ::mockXUngrabServer
        wrapXGetWindowAttributes = ::mockXGetWindowAttributes
        wrapXChangeWindowAttributes = ::mockXChangeWindowAttributes
        wrapXGetTextProperty = ::mockXGetTextProperty
        wrapXGetWindowProperty = ::mockXGetWindowProperty
        wrapXChangeProperty = ::mockXChangeProperty
        wrapXCreateSimpleWindow = ::mockXCreateSimpleWindow
        wrapXDestroyWindow = ::mockXDestroyWindow
        wrapXReparentWindow = ::mockXReparentWindow
        wrapXRestackWindows = ::mockXRestackWindows
        wrapXMapWindow = ::mockXMapWindow
        wrapXUnmapWindow = ::mockXUnmapWindow
        wrapXClearWindow = ::mockXClearWindow
        wrapXMoveWindow = ::mockXMoveWindow
        wrapXResizeWindow = ::mockXResizeWindow
        wrapXMoveResizeWindow = ::mockXMoveResizeWindow
        wrapXSetWindowBorderWidth = ::mockXSetWindowBorderWidth
        wrapXFlush = ::mockXFlush
        wrapXFree = ::mockXFree
        wrapXGetTransientForHint = ::mockXGetTransientForHint
        wrapXSendEvent = ::mockXSendEvent
        wrapXConfigureWindow = ::mockXConfigureWindow
        wrapXSelectInput = ::mockXSelectInput
        wrapXAddToSaveSet = ::mockXAddToSaveSet
        wrapXRemoveFromSaveSet = ::mockXRemoveFromSaveSet
        wrapXKeysymToKeycode = ::mockXKeysymToKeycode
        wrapMqOpen = ::mockMqOpen
        wrapMqClose = ::mockMqClose
        wrapMqSend = ::mockMqSend
        wrapMqReceive = ::mockMqReceive
        wrapMqUnlink = ::mockMqUnlink
    }

    @AfterTest
    open fun teardown() {
        wrapXGrabServer = ::XGrabServer
        wrapXUngrabServer = ::XUngrabServer
        wrapXGetWindowAttributes = ::XGetWindowAttributes
        wrapXChangeWindowAttributes = ::XChangeWindowAttributes
        wrapXGetTextProperty = ::XGetTextProperty
        wrapXGetWindowProperty = ::XGetWindowProperty
        wrapXChangeProperty = ::XChangeProperty
        wrapXCreateSimpleWindow = ::XCreateSimpleWindow
        wrapXDestroyWindow = ::XDestroyWindow
        wrapXReparentWindow = ::XReparentWindow
        wrapXRestackWindows = ::XRestackWindows
        wrapXMapWindow = ::XMapWindow
        wrapXUnmapWindow = ::XUnmapWindow
        wrapXClearWindow = ::XClearWindow
        wrapXMoveWindow = ::XMoveWindow
        wrapXResizeWindow = ::XResizeWindow
        wrapXMoveResizeWindow = ::XMoveResizeWindow
        wrapXSetWindowBorderWidth = ::XSetWindowBorderWidth
        wrapXFlush = ::XFlush
        wrapXFree = ::XFree
        wrapXGetTransientForHint = ::XGetTransientForHint
        wrapXSendEvent = ::XSendEvent
        wrapXConfigureWindow = ::XConfigureWindow
        wrapXSelectInput = ::XSelectInput
        wrapXAddToSaveSet = ::XAddToSaveSet
        wrapXRemoveFromSaveSet = ::XRemoveFromSaveSet
        wrapXKeysymToKeycode = ::XKeysymToKeycode
        wrapMqOpen = ::mqWrapper
        wrapMqClose = ::mq_close
        wrapMqSend = ::mq_send
        wrapMqReceive = ::mq_receive
        wrapMqUnlink = ::mq_unlink
    }
}