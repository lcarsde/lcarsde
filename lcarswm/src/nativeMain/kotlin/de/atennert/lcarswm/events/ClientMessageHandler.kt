package de.atennert.lcarswm.events

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.ClientMessage
import xlib.XEvent

@ExperimentalForeignApi
class ClientMessageHandler(private val logger: Logger, private val atomLibrary: AtomLibrary) : XEventHandler {
    override val xEventType = ClientMessage

    override fun handleEvent(event: XEvent): Boolean {
        val window = event.xclient.window
        val messageType = event.xclient.message_type
        val atom = Atoms.values().find { atomLibrary[it] == messageType }

        logger.logDebug("ClientMessageHandler::handleEvent::window: $window, message type: $messageType, atom: $atom")

        return false
    }
}
