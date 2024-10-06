package de.atennert.lcarswm.log

import de.atennert.lcarswm.file.AccessMode
import de.atennert.lcarswm.file.File
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.time.Time

/**
 * Logger that logs into a file at the given path.
 */
class FileLogger(fileFactory: FileFactory, logFilePath: String, val time: Time) : Logger {
    private val file: File = fileFactory.getFile(logFilePath, AccessMode.WRITE)

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
        logInfo("FileLogger::close::lcarswm stopped $this")
        file.close()
    }
}