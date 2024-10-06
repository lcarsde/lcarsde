package de.atennert.lcarswm.log

import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.time.Time

fun createLogger(fileFactory: FileFactory, logFilePath: String?, time: Time): Logger {
    return object : Logger {
        val internalLogger = setOfNotNull(
            logFilePath?.let { FileLogger(fileFactory, logFilePath, time) },
            PrintLogger()
        )

        override fun logDebug(text: String) {
            internalLogger.forEach { it.logDebug(text) }
        }

        override fun logInfo(text: String) {
            internalLogger.forEach { it.logInfo(text) }
        }

        override fun logWarning(text: String) {
            internalLogger.forEach { it.logWarning(text) }
        }

        override fun logWarning(text: String, throwable: Throwable) {
            internalLogger.forEach { it.logWarning(text, throwable) }
        }

        override fun logError(text: String) {
            internalLogger.forEach { it.logError(text) }
        }

        override fun logError(text: String, throwable: Throwable) {
            internalLogger.forEach { it.logError(text, throwable) }
        }
    }
}
