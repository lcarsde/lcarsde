package de.atennert.lcarswm.keys

import de.atennert.lcarswm.events.EventTime
import de.atennert.lcarswm.system.api.InputApi
import de.atennert.lcarswm.window.FocusObserver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.CurrentTime
import xlib.Time
import xlib.Window

/**
 * Used to grab the key during a focus toggle session
 * so we also get key release events for modifier keys.
 */
@ExperimentalForeignApi
class FocusSessionKeyboardGrabber(
    private val inputApi: InputApi,
    private val eventTime: EventTime,
    private val ewmhSupportWindow: Window
) : FocusObserver {
    private var grabActive = false
    private var grabTime: Time = CurrentTime.convert()

    override fun invoke(activeWindow: Window?, oldWindow: Window?, toggleSessionActive: Boolean) {
        if (toggleSessionActive != grabActive) {
            grabActive = toggleSessionActive

            if (toggleSessionActive) {
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