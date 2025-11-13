package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ClientMessage
import xlib.XEvent

@ExperimentalForeignApi
class ClientMessageHandler(private val atomLibrary: AtomLibrary) : XEventHandler {
    private val logger by inject<Logger>()

    override val xEventType = ClientMessage

    override fun handleEvent(event: XEvent): Boolean {
        val window = event.xclient.window
        val messageType = event.xclient.message_type
        val atom = Atoms.entries.find { atomLibrary[it] == messageType }

        logger.logDebug("ClientMessageHandler::handleEvent::window: $window, message type: $messageType, atom: $atom")

        return false
    }
}
