package de.atennert.lcarswm.file

import de.atennert.lcarsde.file.AccessMode
import de.atennert.lcarsde.file.File
import de.atennert.lcarsde.file.PosixFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.opendir

/**
 * Returns directories using POSIX.
 */
@ExperimentalForeignApi
class PosixFileFactory : FileFactory {
    override fun getDirectory(path: String): Directory? {
        return opendir(path)?.let { PosixDirectory(it) }
    }

    override fun getFile(path: String, accessMode: AccessMode): File {
        return PosixFile(path, accessMode)
    }
}