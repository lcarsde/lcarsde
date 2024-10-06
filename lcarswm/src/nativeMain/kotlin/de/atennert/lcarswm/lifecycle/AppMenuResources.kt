package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.AppMenuMessageHandler
import de.atennert.lcarswm.system.MessageQueue
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
data class AppMenuResources(val messageHandler: AppMenuMessageHandler, val messageQueue: MessageQueue)