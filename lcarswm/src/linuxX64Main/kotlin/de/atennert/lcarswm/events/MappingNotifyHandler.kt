package de.atennert.lcarswm.events

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.keys.KeyConfiguration
import de.atennert.lcarswm.keys.KeyManager
import gtk.MappingNotify
import gtk.Window
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
class MappingNotifyHandler(
    private val keyManager: KeyManager,
    private val keyConfiguration: KeyConfiguration,
    private val rootWindowId: Window
) : XEventHandler {
    private val logger by inject<Logger>()

    override val xEventType = MappingNotify

    override fun handleEvent(event: XEvent): Boolean {
        logger.logDebug("MappingNotifyHandler::handleEvent::reloading keyboard config")
        keyManager.ungrabAllKeys(rootWindowId)
        keyManager.reloadConfig()
        keyConfiguration.reloadConfig()
        return false
    }
}