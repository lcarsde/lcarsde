package de.atennert.lcarswm.lifecycle

import de.atennert.lcarsde.log.Logger
import exitState
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlin.experimental.ExperimentalNativeApi


@ExperimentalNativeApi
@ExperimentalForeignApi
suspend fun runEventLoops(
    logger: Logger,
    runtimeResources: RuntimeResources
) = coroutineScope {

    val sideBarJob = runSideBarLoop(logger, runtimeResources.sideBar)
    val xEventJob = runXEventLoop(logger, runtimeResources.xEvent)

    // When the X event loop goes down, so does everything else
    xEventJob.invokeOnCompletion {
        sideBarJob.cancel()
    }

    sideBarJob.join()
    logger.logDebug("::eventLoop::finished event loops")
}

@ExperimentalForeignApi
private fun CoroutineScope.runXEventLoop(
    logger: Logger,
    xEventResources: XEventResources
): Job {
    val eventBuffer = xEventResources.eventBuffer
    val eventTime = xEventResources.eventTime
    val eventDistributor = xEventResources.eventHandler

    val xEventJob = launch {
        logger.logDebug("::runXEventLoop::running X event loop")
        while (exitState.value == null) {
            val xEvent = eventBuffer.getNextEvent(false)?.pointed

            if (xEvent == null) {
                delay(50)
                continue
            }

            eventTime.setTimeFromEvent(xEvent.ptr)

            if (eventDistributor.handleEvent(xEvent)) {
                exitState.value = 0
            }

            eventTime.unsetEventTime()

            nativeHeap.free(xEvent)
        }
    }
    xEventJob.invokeOnCompletion { logger.logDebug("::runXEventLoop::X event job completed") }
    return xEventJob
}

@ExperimentalNativeApi
@ExperimentalForeignApi
private fun CoroutineScope.runSideBarLoop(
    logger: Logger,
    sideBarResources: SideBarResources
): Job {
    val sideBarMessageQueue = sideBarResources.messageQueue
    val sideBarMessageHandler = sideBarResources.messageHandler

    val sideBarJob = launch {
        logger.logDebug("::runSideBarLoop::running Side Bar job")
        while (true) {
            sideBarMessageQueue.receive()?.let {
                sideBarMessageHandler.handleMessage(it)
            }
            delay(100)
        }
    }
    sideBarJob.invokeOnCompletion { logger.logDebug("::runSideBarLoop::Side Bar job completed") }
    return sideBarJob
}
