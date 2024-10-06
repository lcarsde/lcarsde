package de.atennert.lcarswm.window

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.system.wrapXRestackWindows
import de.atennert.rx.NextObserver
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import xlib.Display
import xlib.Window

@ExperimentalForeignApi
class WindowStack(
    private val display: CPointer<Display>?,
    windowList: WindowList,
    focusHandler: WindowFocusHandler
) {
    private val stack = mutableListOf<ManagedWmWindow<Window>>()
    private var activeWindow: Window? = null

    private val comparator = Comparator<ManagedWmWindow<Window>> { a, b ->
        val aValue = getWindowValue(a)
        val bValue = getWindowValue(b)

        bValue - aValue
    }

    init {
        windowList.windowEventObs
            .subscribe(NextObserver {
                if (it is WindowAddedEvent) {
                    stack.add(0, it.window)
                } else if (it is WindowRemovedEvent) {
                    stack.remove(it.window)
                }
                stack.sortWith(comparator)
                triggerWmRestack()
            })
            .closeWith { this.unsubscribe() }

        focusHandler.windowFocusEventObs
            .subscribe(NextObserver { (newWindow) ->
                activeWindow = newWindow
                if (newWindow == null) {
                    return@NextObserver
                }
                stack.find { it.id == newWindow }
                    ?.let {
                        stack.remove(it)
                        stack.add(0, it)
                        stack.sortWith(comparator)
                        triggerWmRestack()
                    }
            })
            .closeWith { this.unsubscribe() }
    }

    private fun getWindowValue(a: ManagedWmWindow<Window>): Int {
        var value = when {
            a is PosixTransientWindow && a.isTransientForRoot -> 100
//            a is PosixTransientWindow -> 10 // TODO make sure that transient windows are only on top of their parents
            else -> 0
        }
        value += if (a.id == activeWindow) 1 else 0
        return value
    }

    private fun triggerWmRestack() {
        if (stack.isEmpty()) {
            return
        }
        stack.map { if (it is PosixTransientWindow && it.isTransientForRoot) it.id else it.frame }
            .toULongArray()
            .usePinned {
                wrapXRestackWindows(display, it.addressOf(0), it.get().size)
            }
    }
}