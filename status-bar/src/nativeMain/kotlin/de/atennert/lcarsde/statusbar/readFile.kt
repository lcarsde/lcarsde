package de.atennert.lcarsde.statusbar

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned

@ExperimentalForeignApi
fun readFile(path: String): String? {
    platform.posix.fopen(path, "r")?. let { fp ->
        var s = ""
        val buf = ByteArray(1000)
        buf.usePinned {
            while (platform.posix.fgets(it.addressOf(0), buf.size, fp) != null) {
                s += it.get().toKString()
            }
        }
        platform.posix.fclose(fp)
        return s.trim()
    }
    return null
}
