package de.atennert.lcarswm.system

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.linux.*
import xlib.*

@ExperimentalForeignApi
var wrapXGrabServer = ::XGrabServer

@ExperimentalForeignApi
var wrapXUngrabServer = ::XUngrabServer

@ExperimentalForeignApi
var wrapXGetWindowAttributes = ::XGetWindowAttributes

@ExperimentalForeignApi
var wrapXChangeWindowAttributes = ::XChangeWindowAttributes

@ExperimentalForeignApi
var wrapXGetTextProperty = ::XGetTextProperty

@ExperimentalForeignApi
var wrapXGetWindowProperty = ::XGetWindowProperty

@ExperimentalForeignApi
var wrapXChangeProperty = ::XChangeProperty

@ExperimentalForeignApi
var wrapXCreateSimpleWindow = ::XCreateSimpleWindow

@ExperimentalForeignApi
var wrapXDestroyWindow = ::XDestroyWindow

@ExperimentalForeignApi
var wrapXReparentWindow = ::XReparentWindow

@ExperimentalForeignApi
var wrapXRestackWindows = ::XRestackWindows

@ExperimentalForeignApi
var wrapXMapWindow = ::XMapWindow

@ExperimentalForeignApi
var wrapXUnmapWindow = ::XUnmapWindow

@ExperimentalForeignApi
var wrapXClearWindow = ::XClearWindow

@ExperimentalForeignApi
var wrapXMoveWindow = ::XMoveWindow

@ExperimentalForeignApi
var wrapXResizeWindow = ::XResizeWindow

@ExperimentalForeignApi
var wrapXMoveResizeWindow = ::XMoveResizeWindow

@ExperimentalForeignApi
var wrapXSetWindowBorderWidth = ::XSetWindowBorderWidth

@ExperimentalForeignApi
var wrapXFlush = ::XFlush

@ExperimentalForeignApi
var wrapXCreatePixmap = ::XCreatePixmap

@ExperimentalForeignApi
var wrapXFreePixmap = ::XFreePixmap

@ExperimentalForeignApi
var wrapXFree = ::XFree

@ExperimentalForeignApi
var wrapXSetWindowBackgroundPixmap = ::XSetWindowBackgroundPixmap

@ExperimentalForeignApi
var wrapXGetTransientForHint = ::XGetTransientForHint

@ExperimentalForeignApi
var wrapXSendEvent = ::XSendEvent

@ExperimentalForeignApi
var wrapXConfigureWindow = ::XConfigureWindow

@ExperimentalForeignApi
var wrapXSelectInput = ::XSelectInput

@ExperimentalForeignApi
var wrapXAddToSaveSet = ::XAddToSaveSet

@ExperimentalForeignApi
var wrapXRemoveFromSaveSet = ::XRemoveFromSaveSet

@ExperimentalForeignApi
var wrapXKeysymToKeycode = ::XKeysymToKeycode

@ExperimentalForeignApi
var wrapXftDrawCreate = ::XftDrawCreate

@ExperimentalForeignApi
var wrapXftDrawDestroy = ::XftDrawDestroy

@ExperimentalForeignApi
var wrapXftDrawRect = ::XftDrawRect

@ExperimentalForeignApi
var wrapPangoLayoutSetText = ::pango_layout_set_text

@ExperimentalForeignApi
var wrapPangoLayoutSetWidth = ::pango_layout_set_width

@ExperimentalForeignApi
var wrapPangoLayoutGetPixelExtents = ::pango_layout_get_pixel_extents

@ExperimentalForeignApi
var wrapPangoLayoutGetLineReadonly = ::pango_layout_get_line_readonly

@ExperimentalForeignApi
var wrapPangoXftRenderLayoutLine = ::pango_xft_render_layout_line

@ExperimentalForeignApi
var wrapMqOpen = ::mqWrapper
@ExperimentalForeignApi
fun mqWrapper(name: String?, oflag: Int, permissions: mode_t, attributes: mq_attr): mqd_t {
    // this is required because Kotlin/Native doesn't like references to functions with varargs
    return mq_open(name, oflag, permissions, attributes.ptr)
}

var wrapMqClose = ::mq_close

var wrapMqSend = ::mq_send

@ExperimentalForeignApi
var wrapMqReceive = ::mq_receive

var wrapMqUnlink = ::mq_unlink
