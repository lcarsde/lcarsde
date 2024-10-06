package de.atennert.lcarswm.log

class PrintLogger : Logger {
    override fun logDebug(text: String) {
        writeLog("DEBUG", text)
    }

    override fun logInfo(text: String) {
        writeLog("INFO", text)
    }

    override fun logWarning(text: String) {
        writeLog("WARN", text)
    }

    override fun logWarning(text: String, throwable: Throwable) {
        writeLog("WARN", "$text: ${throwable.message}")
    }

    override fun logError(text: String) {
        writeLog("ERROR", text)
    }

    override fun logError(text: String, throwable: Throwable) {
        writeLog("ERROR", "$text: ${throwable.message}")
    }

    private fun writeLog(prefix: String, text: String) {
        println("$prefix: $text")
    }
}