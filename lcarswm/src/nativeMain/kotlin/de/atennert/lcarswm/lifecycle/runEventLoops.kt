package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.log.Logger
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

    val appMenuJob = runAppMenuLoop(logger, runtimeResources.appMenu)
    val xEventJob = runXEventLoop(logger, runtimeResources.xEvent)

    // When the X event loop goes down, so does everything else
    xEventJob.invokeOnCompletion {
        appMenuJob.cancel()
    }

    appMenuJob.join()
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
private fun CoroutineScope.runAppMenuLoop(
    logger: Logger,
    appMenuResources: AppMenuResources
): Job {
    val appMenuMessageQueue = appMenuResources.messageQueue
    val appMenuMessageHandler = appMenuResources.messageHandler

    val appMenuJob = launch {
        logger.logDebug("::runAppMenuLoop::running app menu job")
        while (true) {
            appMenuMessageQueue.receive()?.let {
                appMenuMessageHandler.handleMessage(it)
            }
            delay(100)
        }
    }
    appMenuJob.invokeOnCompletion { logger.logDebug("::runAppMenuLoop::app menu job completed") }
    return appMenuJob
}
