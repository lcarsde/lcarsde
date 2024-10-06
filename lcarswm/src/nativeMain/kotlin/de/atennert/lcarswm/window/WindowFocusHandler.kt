package de.atennert.lcarswm.window

import de.atennert.lcarswm.AppMenuMessageHandler
import de.atennert.lcarswm.keys.KeySessionManager
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.rx.NextObserver
import de.atennert.rx.ReplaySubject
import de.atennert.rx.operators.filter
import de.atennert.rx.operators.withLatestFrom
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window

@ExperimentalForeignApi
typealias FocusObserver = (Window?, Window?, Boolean) -> Unit
@ExperimentalForeignApi
data class WindowFocusEvent(val newWindow: Window?, val oldWindow: Window?, val toggleSessionActive: Boolean)

@ExperimentalForeignApi
class WindowFocusHandler(windowList: WindowList, appMenuMessageHandler: AppMenuMessageHandler) {
    private val windowFocusEventSj = ReplaySubject<WindowFocusEvent>(0)
    val windowFocusEventObs = windowFocusEventSj.asObservable()

    private var activeWindow: Window? = null

    private val windowIdList = mutableListOf<Window>()

    private val observers = mutableListOf<FocusObserver>()

    private var toggleSessionActive = false

    init {
        windowList.windowEventObs
            .apply(filter { it.window !is PosixTransientWindow || !it.window.isTransientForRoot })
            .subscribe(NextObserver {
                when (it) {
                    is WindowAddedEvent -> setFocusedWindow(it.window.id)
                    is WindowRemovedEvent -> removeWindow(it.window.id)
                    is WindowUpdatedEvent -> { /* Nothing to do */ }
                }
            })
            .closeWith { this.unsubscribe() }

        appMenuMessageHandler.selectAppObs
            .apply(withLatestFrom(windowList.windowsObs))
            .apply(filter { (selectedWindowId, windows) ->
                windows.find { it.id == selectedWindowId && (it !is PosixTransientWindow || !it.isTransientForRoot) } != null
            })
            .subscribe(NextObserver { (selectedWindowId) -> setFocusedWindow(selectedWindowId)})
            .closeWith { this.unsubscribe() }
    }

    val keySessionListener = object : KeySessionManager.KeySessionListener {
        override fun stopSession() {
            if (!toggleSessionActive) {
                return
            }
            toggleSessionActive = false
            activeWindow?.let {
                if (windowIdList[0] != it) {
                    putActiveWindowToFront()
                }
            }
            observers.forEach { it(activeWindow, null, false) }
            windowFocusEventSj.next(WindowFocusEvent(activeWindow, null, false))
        }
    }

    fun getFocusedWindow(): Window? {
        return activeWindow
    }

    fun registerObserver(observer: FocusObserver) {
        this.observers.add(observer)
        observer(activeWindow, null, false)
    }

    fun setFocusedWindow(activeWindow: Window) {
        setFocusedWindowInternal(activeWindow)
        putActiveWindowToFront()
    }

    private fun setFocusedWindowInternal(activeWindow: Window) {
        if (activeWindow == this.activeWindow) {
            return
        }

        val oldActiveWindow = this.activeWindow
        this.activeWindow = activeWindow
        this.observers.forEach { it(activeWindow, oldActiveWindow, toggleSessionActive) }
        windowFocusEventSj.next(WindowFocusEvent(activeWindow, oldActiveWindow, toggleSessionActive))
        if (!windowIdList.contains(activeWindow)) {
            windowIdList.add(activeWindow)
        }
    }

    private fun removeWindow(window: Window) {
        windowIdList.remove(window)
        if (window == activeWindow) {
            this.activeWindow = windowIdList.getOrNull(0)
            this.observers.forEach { it(this.activeWindow, window, false) }
            windowFocusEventSj.next(WindowFocusEvent(this.activeWindow, window, false))
        }
    }

    fun toggleWindowFocusForward() {
        if (activeWindow == null) {
            return
        }
        toggleSessionActive = true
        val focusIndex = getIndexOfFocusedWindow()
        val nextWindow = getNextWindowToFocus(focusIndex)
        setFocusedWindowInternal(nextWindow)
    }

    fun toggleWindowFocusBackward() {
        if (activeWindow == null) {
            return
        }
        toggleSessionActive = true
        val focusIndex = getIndexOfFocusedWindow()
        val previousWindow = getPreviousWindowToFocus(focusIndex)
        setFocusedWindowInternal(previousWindow)
    }

    /**
     * When starting a new toggle session, the list should be ordered to that the active window is at the front.
     */
    private fun putActiveWindowToFront() {
        activeWindow?.let {
            windowIdList.remove(it)
            windowIdList.add(0, it)
        }
    }

    private fun getIndexOfFocusedWindow(): Int {
        return windowIdList.indexOf(activeWindow)
    }

    private fun getNextWindowToFocus(currentFocusIndex: Int): Window {
        val nextIndex = (currentFocusIndex + 1).rem(windowIdList.size)
        return windowIdList.elementAt(nextIndex)
    }

    private fun getPreviousWindowToFocus(currentFocusIndex: Int): Window {
        val previousIndex = if (currentFocusIndex - 1 == -1) {
            windowIdList.size - 1
        } else {
            currentFocusIndex - 1
        }
        return windowIdList.elementAt(previousIndex)
    }
}
