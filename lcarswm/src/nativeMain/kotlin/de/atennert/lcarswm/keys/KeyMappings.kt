package de.atennert.lcarswm.keys

import de.atennert.lcarswm.Environment
import de.atennert.lcarswm.system.wrapXKeysymToKeycode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.*

@OptIn(ExperimentalForeignApi::class)
actual const val keyShiftL = XK_Shift_L
@OptIn(ExperimentalForeignApi::class)
actual const val keyShiftR = XK_Shift_R
@OptIn(ExperimentalForeignApi::class)
actual const val keyControlL = XK_Control_L
@OptIn(ExperimentalForeignApi::class)
actual const val keyControlR = XK_Control_R
@OptIn(ExperimentalForeignApi::class)
actual const val keySuperL = XK_Super_L
@OptIn(ExperimentalForeignApi::class)
actual const val keySuperR = XK_Super_R
@OptIn(ExperimentalForeignApi::class)
actual const val keyHyperL = XK_Hyper_L
@OptIn(ExperimentalForeignApi::class)
actual const val keyHyperR = XK_Hyper_R
@OptIn(ExperimentalForeignApi::class)
actual const val keyMetaL = XK_Meta_L
@OptIn(ExperimentalForeignApi::class)
actual const val keyMetaR = XK_Meta_R
@OptIn(ExperimentalForeignApi::class)
actual const val keyAltL = XK_Alt_L
@OptIn(ExperimentalForeignApi::class)
actual const val keyAltR = XK_Alt_R

@OptIn(ExperimentalForeignApi::class)
actual fun keysymToKeycode(env: Environment, keySym: Int): UInt {
    return wrapXKeysymToKeycode(env.display, keySym.convert()).convert()
}
