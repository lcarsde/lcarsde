package de.atennert.lcarswm.window

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarswm.events.EventTime
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.system.api.InputApi
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RevertToNone
import xlib.RevertToPointerRoot
import xlib.Window

/**
 * Sets the input focus based on the current window focus.
 */
@ExperimentalForeignApi
class InputFocusHandler(
    private val inputApi: InputApi,
    private val eventTime: EventTime,
    private val rootWindow: Window
) : FocusObserver {
    private val logger by inject<Logger>()

    override fun invoke(activeWindow: Window?, oldWindow: Window?, toggleSessionActive: Boolean) {
        if (!toggleSessionActive) {
            if (activeWindow != null) {
                logger.logDebug("::startup::set input focus to $activeWindow")
                inputApi.setInputFocus(activeWindow, RevertToNone, eventTime.lastEventTime)
            } else {
                logger.logDebug("::startup::set input focus to root")
                inputApi.setInputFocus(rootWindow, RevertToPointerRoot, eventTime.lastEventTime)
            }
        }
    }
}