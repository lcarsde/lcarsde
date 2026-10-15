package de.atennert.lcarswm.keys

import de.atennert.lcarswm.events.EventTime
import de.atennert.lcarswm.system.api.InputApi
import de.atennert.lcarswm.window.FocusObserver
import gtk.CurrentTime
import gtk.Time
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert

/**
 * Used to grab the key during a Window Cycling
 * so we also get key release events for modifier keys.
 */
@ExperimentalForeignApi
class FocusCyclingKeyboardGrabber(
    private val inputApi: InputApi,
    private val eventTime: EventTime,
    private val ewmhSupportWindow: Window
) : FocusObserver {
    private var grabActive = false
    private var grabTime: Time = CurrentTime.convert()

    override fun invoke(activeWindow: Window?, oldWindow: Window?, cyclingActive: Boolean) {
        if (cyclingActive != grabActive) {
            grabActive = cyclingActive

            if (cyclingActive) {
                grabTime = eventTime.lastEventTime
                inputApi.grabKeyboard(ewmhSupportWindow, grabTime)
            } else {
                inputApi.ungrabKeyboard(
                    if (eventTime.lastEventTime > grabTime)
                        eventTime.lastEventTime
                    else
                        CurrentTime.convert()
                )
            }
        }
    }
}