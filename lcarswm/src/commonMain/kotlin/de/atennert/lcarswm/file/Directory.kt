package de.atennert.lcarswm.file

/**
 * Base definition for handling directories.
 */
interface Directory {
    fun readFiles(): Set<String>

    fun close()
}