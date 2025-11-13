package de.atennert.lcarswm.file

import de.atennert.lcarsde.file.AccessMode
import de.atennert.lcarsde.file.File

/**
 * Used to get instances of Directory.
 */
interface FileFactory {
    fun getDirectory(path: String): Directory?

    fun getFile(path: String, accessMode: AccessMode): File
}