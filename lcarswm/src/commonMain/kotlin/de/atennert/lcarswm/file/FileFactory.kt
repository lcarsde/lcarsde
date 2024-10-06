package de.atennert.lcarswm.file

/**
 * Used to get instances of Directory.
 */
interface FileFactory {
    fun getDirectory(path: String): Directory?

    fun getFile(path: String, accessMode: AccessMode): File
}