package de.atennert.lcarsde.log

import de.atennert.lcarsde.file.AccessMode
import de.atennert.lcarsde.file.File
import de.atennert.lcarsde.file.Files
import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarsde.time.Time

/**
 * Logger that logs into a file at the given path.
 */
class FileLogger(files: Files, logFilePath: String, val time: Time) : Logger {
    private val file: File = files.open(logFilePath, AccessMode.WRITE)

    init {
        closeWith(FileLogger::close)
    }

    override fun logDebug(text: String) {
        writeLog("DEBUG", text)
    }

    override fun logInfo(text: String) {
        writeLog(" INFO", text)
    }

    override fun logWarning(text: String) {
        writeLog(" WARN", text)
    }

    override fun logWarning(text: String, throwable: Throwable) {
        writeLog(" WARN", "$text: ${throwable.message}\n${throwable.stackTraceToString()}")
    }

    override fun logError(text: String) {
        writeLog("ERROR", text)
    }

    override fun logError(text: String, throwable: Throwable) {
        writeLog("ERROR", "$text: ${throwable.message}\n${throwable.stackTraceToString()}")
    }

    private fun writeLog(prefix: String, text: String) {
        file.writeLine("${time.getTime("%Y-%m-%d %H:%M:%S")} - $prefix: $text")
    }

    private fun close() {
        logDebug("FileLogger::close")
        file.close()
    }
}