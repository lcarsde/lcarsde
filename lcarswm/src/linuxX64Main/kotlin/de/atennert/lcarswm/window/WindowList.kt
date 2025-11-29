package de.atennert.lcarswm.window

import de.atennert.rx.BehaviorSubject
import de.atennert.rx.Subject
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window

@ExperimentalForeignApi
sealed class WindowEvent(val window: ManagedWmWindow<Window>)
@ExperimentalForeignApi
class WindowAddedEvent(window: ManagedWmWindow<Window>) : WindowEvent(window)
@ExperimentalForeignApi
class WindowUpdatedEvent(window: ManagedWmWindow<Window>) : WindowEvent(window)
@ExperimentalForeignApi
class WindowRemovedEvent(window: ManagedWmWindow<Window>) : WindowEvent(window)

class WindowList {
    @ExperimentalForeignApi
    private val windowsSj = BehaviorSubject<Set<ManagedWmWindow<Window>>>(emptySet())
    @ExperimentalForeignApi
    val windowsObs = windowsSj.asObservable()
    @ExperimentalForeignApi
    private var windows by windowsSj

    @ExperimentalForeignApi
    private val windowEventSj = Subject<WindowEvent>()
    @ExperimentalForeignApi
    val windowEventObs = windowEventSj.asObservable()

    @ExperimentalForeignApi
    fun add(window: ManagedWmWindow<Window>) {
        windows += window
        windowEventSj.next(WindowAddedEvent(window))
    }

    @ExperimentalForeignApi
    fun remove(window: ManagedWmWindow<Window>) {
        windows -= window
        windowEventSj.next(WindowRemovedEvent(window))
    }

    @ExperimentalForeignApi
    fun remove(windowId: Window): ManagedWmWindow<Window>? {
        val window = get(windowId) ?: return null
        remove(window)
        return window
    }

    @ExperimentalForeignApi
    fun get(windowId: Window): ManagedWmWindow<Window>? {
        return windows.firstOrNull { it.id == windowId }
    }

    @ExperimentalForeignApi
    fun getByAny(windowId: Window): ManagedWmWindow<Window>? {
        return windows.firstOrNull { it.hasId(windowId) }
    }

    @ExperimentalForeignApi
    fun isManaged(windowId: Window): Boolean {
        return windows.any { it.id == windowId }
    }
}