package de.atennert.lcarswm.log

import de.atennert.lcarsde.file.Files
import de.atennert.lcarsde.log.FileLogger
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarsde.log.PrintLogger
import de.atennert.lcarsde.time.Time

fun createLogger(files: Files, logFilePath: String?, time: Time): Logger {
    return object : Logger {
        val internalLogger = setOfNotNull(
            logFilePath?.let { FileLogger(files, logFilePath, time) },
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
