package de.atennert.lcarswm.events

import de.atennert.rx.Subject
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window
import xlib.XConfigureRequestEvent

@ExperimentalForeignApi
data class ReparentEvent(val id: Window, val parentId: Window)

class EventStore {
    @ExperimentalForeignApi
    internal val destroySj = Subject<Window>()
    @ExperimentalForeignApi
    val destroyObs = destroySj.asObservable()

    @ExperimentalForeignApi
    internal val mapSj = Subject<Window>()
    @ExperimentalForeignApi
    val mapObs = mapSj.asObservable()

    @ExperimentalForeignApi
    internal val unmapSj = Subject<Window>()
    @ExperimentalForeignApi
    val unmapObs = unmapSj.asObservable()

    @ExperimentalForeignApi
    internal val reparentSj = Subject<ReparentEvent>()
    @ExperimentalForeignApi
    val reparentObs = reparentSj.asObservable()

    @ExperimentalForeignApi
    internal val propertyNotifyNameSj = Subject<Window>()
    @ExperimentalForeignApi
    val propertyNotifyNameObs = propertyNotifyNameSj.asObservable()

    @ExperimentalForeignApi
    internal val configureRequestSj = Subject<XConfigureRequestEvent>()
    @ExperimentalForeignApi
    val configureRequestObs = configureRequestSj.asObservable()

    @ExperimentalForeignApi
    internal val enterNotifySj = Subject<Window>()
    @ExperimentalForeignApi
    val enterNotifyObs = enterNotifySj.asObservable()

    @ExperimentalForeignApi
    internal val leaveNotifySj = Subject<Window>()
    @ExperimentalForeignApi
    val leaveNotifyObs = leaveNotifySj.asObservable()
}