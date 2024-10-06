package de.atennert.lcarswm.log

/**
 * Interface for logger implementations
 */
interface Logger {
    /** log information in debug mode */
    fun logDebug(text: String)

    /** log information in info mode */
    fun logInfo(text: String)

    /** log information in warning mode */
    fun logWarning(text: String)

    /** log information in warning mode */
    fun logWarning(text: String, throwable: Throwable)

    /** log information in error mode */
    fun logError(text: String)

    /**
     * log information in error mode with throwable
     */
    fun logError(text: String, throwable: Throwable)
}