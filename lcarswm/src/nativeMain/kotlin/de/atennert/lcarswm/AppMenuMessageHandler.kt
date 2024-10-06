package de.atennert.lcarswm

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.system.api.SystemApi
import de.atennert.lcarswm.window.WindowList
import de.atennert.lcarswm.window.closeWindow
import de.atennert.rx.Subject
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window

@ExperimentalForeignApi
class AppMenuMessageHandler(
    private val logger: Logger,
    private val systemApi: SystemApi,
    private val atomLibrary: AtomLibrary,
    private val windowList: WindowList,
) {
    private val selectAppSj = Subject<Window>()
    val selectAppObs = selectAppSj.asObservable()

    private val messageHandlers = mapOf(
        "close" to this::handleClose,
        "select" to this::handleSelection,
    )

    fun handleMessage(message: String) {
        val messageLines = message.lines()
        val messageType = messageLines[0]
        val messageContent = messageLines.drop(1)

        messageHandlers[messageType]?.invoke(messageContent)
            ?: logger.logWarning("AppMenuMessageHandler::handleMessage::unknown message type $messageType")
    }

    private fun handleClose(message: List<String>) {
        val targetWindowId = message[0].toULong()
        logger.logDebug("AppMenuMessageHandler::handleClose::$targetWindowId")

        if (windowList.isManaged(targetWindowId)) {
            closeWindow(logger, systemApi, atomLibrary, targetWindowId)
        }
    }

    private fun handleSelection(message: List<String>) {
        val targetWindow = message[0].toULong()
        logger.logDebug("AppMenuMessageHandler::handleSelection::$targetWindow")

        selectAppSj.next(targetWindow)
    }
}