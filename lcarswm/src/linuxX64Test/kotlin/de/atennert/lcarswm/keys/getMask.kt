package de.atennert.lcarswm.keys

import kotlinx.cinterop.ExperimentalForeignApi
import xlib.AnyModifier
import xlib.ControlMask
import xlib.LockMask
import xlib.ShiftMask

@ExperimentalForeignApi
fun getMask(l: List<Modifiers>): Int {
    val mask = l.fold(0) { acc, m ->
        acc or when (m) {
            Modifiers.SHIFT -> ShiftMask
            Modifiers.CAPS_LOCK -> LockMask
            Modifiers.CONTROL -> ControlMask
            Modifiers.NUM_LOCK -> 0
            Modifiers.ALT -> 0x08
            Modifiers.HYPER -> 0x10
            Modifiers.META -> 0x20
            Modifiers.SUPER -> 0x40
            Modifiers.SCROLL_LOCK -> 0x80
        }
    }
    return if (mask == 0) {
        AnyModifier
    } else {
        mask
    }
}
