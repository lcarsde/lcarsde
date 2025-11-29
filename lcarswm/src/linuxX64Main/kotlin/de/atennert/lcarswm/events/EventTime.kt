package de.atennert.lcarswm.events

import de.atennert.lcarswm.RootWindowPropertyHandler
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.system.api.WindowUtilApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import xlib.*

/**
 * Tracks the time of events.
 */
@ExperimentalForeignApi
class EventTime(
    private val windowUtilApi: WindowUtilApi,
    private val eventBuffer: EventBuffer,
    private val atomLibrary: AtomLibrary,
    private val rootWindowPropertyHandler: RootWindowPropertyHandler
) {
    private var _lastEventTime: Time = CurrentTime.convert()
    /** The last known event time ... use to trigger other stuff */
    val lastEventTime: Time
        get() {
            if (_lastEventTime == CurrentTime.convert<Time>()) {
                triggerTimeUpdate()
            }
            return _lastEventTime
        }

    private fun triggerTimeUpdate() {
        windowUtilApi.changeProperty(
            rootWindowPropertyHandler.ewmhSupportWindow,
            atomLibrary[Atoms.WM_CLASS],
            atomLibrary[Atoms.STRING],
            null,
            8,
            PropModeAppend
        )
        eventBuffer.findEvent(true) { this.findEvent(it) }
    }

    /**
     * Reset the known last event time.
     */
    fun resetEventTime() {
        _lastEventTime == CurrentTime.convert<Time>()
    }

    fun unsetEventTime() {
        _lastEventTime = CurrentTime.convert()
    }

    private fun findEvent(event: CPointer<XEvent>): Boolean {
        val newTime = getTimeFromEvent(event)
        return if (newTime != CurrentTime.convert<Time>() && newTime != _lastEventTime) {
            _lastEventTime = newTime
            true
        } else {
            false
        }
    }

    /**
     * Set the last event time based on the given event.
     */
    fun setTimeFromEvent(event: CPointer<XEvent>) {
        _lastEventTime = getTimeFromEvent(event)
    }

    private fun getTimeFromEvent(event: CPointer<XEvent>): Time {
        return when (event.pointed.type) {
            ButtonPress -> event.pointed.xbutton.time
            ButtonRelease -> event.pointed.xbutton.time
            KeyPress -> event.pointed.xkey.time
            KeyRelease -> event.pointed.xkey.time
            MotionNotify -> event.pointed.xmotion.time
            PropertyNotify -> event.pointed.xproperty.time
            EnterNotify -> event.pointed.xcrossing.time
            LeaveNotify -> event.pointed.xcrossing.time
            else -> CurrentTime.convert()
        }
    }
}
