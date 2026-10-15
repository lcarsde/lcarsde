package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import gtk.SelectionClear
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

class SelectionClearHandler : XEventHandler {
    private val logger by inject<Logger>()

    @OptIn(ExperimentalForeignApi::class)
    override val xEventType = SelectionClear

    @OptIn(ExperimentalForeignApi::class)
    override fun handleEvent(event: XEvent): Boolean {
        logger.logInfo("SelectionClearHandler::handleEvent::triggering shutdown - other WM calls")
        return true
    }
}