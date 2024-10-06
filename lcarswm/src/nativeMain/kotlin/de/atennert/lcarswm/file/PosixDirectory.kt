package de.atennert.lcarswm.file

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.DIR
import platform.posix.closedir
import platform.posix.readdir

/**
 * Handles directories using POSIX.
 */
@ExperimentalForeignApi
class PosixDirectory(private val dir: CPointer<DIR>) : Directory {
    override fun readFiles(): Set<String> {
        val files = mutableSetOf<String>()
        while (true) {
            val fileEntry = readdir(dir) ?: break // TODO check if it was an error
            files.add(fileEntry.pointed.d_name.toKString())
        }
        return files
    }

    override fun close() {
        closedir(dir)
    }
}