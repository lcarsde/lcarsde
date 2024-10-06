package de.atennert.lcarswm.events

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.SelectionClear
import xlib.XEvent

class SelectionClearHandler(private val logger: Logger) : XEventHandler {
    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = SelectionClear

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        logger.logInfo("SelectionClearHandler::handleEvent::triggering shutdown - other WM calls")
        return true
    }
}