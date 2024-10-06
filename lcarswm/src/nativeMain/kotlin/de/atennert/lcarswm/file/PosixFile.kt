package de.atennert.lcarswm.file

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.*

@ExperimentalForeignApi
class PosixFile(
    path: String,
    accessMode: AccessMode
) : File {
    private val file: CPointer<FILE> = fopen(path, accessMode.posixCode)
        ?: error("PosixFile::init::unable to open file $path")

    override fun write(text: String) {
        fputs(text, file)
        fflush(file)
    }

    override fun writeLine(text: String) {
        this.write("$text\n")
    }

    override fun close() {
        fclose(file)
    }
}