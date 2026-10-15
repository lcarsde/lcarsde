package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.SideBarMessageHandler
import de.atennert.lcarsde.comm.MessageQueue
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
data class SideBarResources(val messageHandler: SideBarMessageHandler, val messageQueue: MessageQueue)