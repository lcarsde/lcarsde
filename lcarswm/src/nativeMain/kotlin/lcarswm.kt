import de.atennert.lcarswm.HOME_CACHE_DIR_PROPERTY
import de.atennert.lcarswm.LOG_FILE_PATH
import de.atennert.lcarswm.PosixResourceGenerator
import de.atennert.lcarswm.ResourceGenerator
import de.atennert.lcarswm.lifecycle.*
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.log.createLogger
import de.atennert.lcarswm.system.SystemFacade
import de.atennert.lcarswm.system.api.SystemApi
import de.atennert.lcarswm.time.PosixTime
import de.atennert.rx.NextObserver
import de.atennert.rx.operators.filterNotNull
import de.atennert.rx.operators.take
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import platform.posix.getenv
import kotlin.experimental.ExperimentalNativeApi
import kotlin.system.exitProcess

// this is a somewhat dirty hack to hand the logger to staticCFunction as error handler
var staticLogger: Logger? = null

/**
 * Some shitty global state variable that is used in different places to handle exiting the main event loop and hold
 * the exit value for the application
 */
val exitState = atomic<Int?>(null)

// the main method apparently must not be inside a package, so it can be compiled with Kotlin/Native
@ExperimentalNativeApi
@ExperimentalForeignApi
fun main() = runBlocking {
    val system = SystemFacade()
    val cacheDirPath = getenv(HOME_CACHE_DIR_PROPERTY)?.toKString()?.plus(LOG_FILE_PATH)
    val resourceGenerator = PosixResourceGenerator()
    val logger = createLogger(resourceGenerator.createFileFactory(), cacheDirPath, PosixTime())

    runWindowManager(system, logger, resourceGenerator)

    if (exitState.value != 0) {
        exitProcess(exitState.value ?: -1)
    }
}

@ExperimentalNativeApi
@ExperimentalForeignApi
suspend fun runWindowManager(system: SystemApi, logger: Logger, resourceGenerator: ResourceGenerator) = coroutineScope {
    logger.logInfo("::runWindowManager::start lcarswm initialization")
    exitState.value = null
    staticLogger = logger

    val runtimeResources: RuntimeResources? = try {
        startup(system, logger, resourceGenerator)
    } catch (e: Throwable) {
        logger.logError("::runWindowManager::error starting lcarswm", e)
        null
    }

    runtimeResources?.let { rr ->
        try {
            (rr.platform.monitorManager.primaryMonitorObs)
                .apply(filterNotNull())
                .apply(take(1))
                .subscribe(NextObserver {
                    runAutostartApps(
                        rr.platform.environment,
                        rr.platform.fileFactory,
                        rr.platform.commander,
                        rr.platform.files,
                        logger
                    )
                })
        } catch (e: Throwable) {
            logger.logError("::runWindowManager::error starting applications", e)
        }

        try {
            runEventLoops(logger, rr)
        } catch (e: Throwable) {
            logger.logError("::runWindowManager::error during runtime", e)
        }
    }

    shutdown(system)
}
