package de.atennert.lcarswm.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.*

@ExperimentalForeignApi
class PosixFiles : Files {
    private val readBufferSize = 60

    override fun exists(path: String): Boolean {
        return access(path, F_OK) == 0
    }

    override fun readLines(path: String, consumer: (String) -> Unit) {
        val filePointer = fopen(path, "r") ?: return

        var bufferString = ""

        ByteArray(readBufferSize).usePinned { entry ->
            while (fgets(entry.addressOf(0), readBufferSize, filePointer) != null) {
                bufferString += entry.get()
                    .takeWhile { it > 0 }
                    .fold("") {acc, b -> acc + b.toInt().toChar()}

                if (bufferString.endsWith('\n') || feof(filePointer) != 0) {
                    consumer(bufferString.trimEnd('\r', '\n'))
                    bufferString = ""
                }
            }
        }

        fclose(filePointer)
    }
}