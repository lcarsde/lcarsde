package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.events.EventBuffer
import de.atennert.lcarswm.events.EventDistributor
import de.atennert.lcarswm.events.EventTime
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
data class XEventResources(val eventHandler: EventDistributor, val eventTime: EventTime, val eventBuffer: EventBuffer)