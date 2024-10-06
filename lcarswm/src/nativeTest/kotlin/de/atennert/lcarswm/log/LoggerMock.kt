package de.atennert.lcarswm.log

/**
 *
 */
class LoggerMock : Logger {
    var closed = false

    override fun logDebug(text: String) {}

    override fun logInfo(text: String) {}

    override fun logWarning(text: String) {}

    override fun logWarning(text: String, throwable: Throwable) {}

    override fun logError(text: String) {}

    override fun logError(text: String, throwable: Throwable) {}

    fun close() {
        this.closed = true
    }
}