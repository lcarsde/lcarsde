package de.atennert.gtk

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/** Overrides the lcarsde data directory, e.g. to run Tool Apps from the build output. */
const val DATA_DIR_PROPERTY = "LCARSDE_DATA_DIR"

const val DEFAULT_DATA_DIR = "/usr/share/lcarsde"

/**
 * @return the absolute path of a file in the lcarsde data directory
 */
@OptIn(ExperimentalForeignApi::class)
fun dataPath(relativePath: String): String {
    val dataDir = getenv(DATA_DIR_PROPERTY)?.toKString() ?: DEFAULT_DATA_DIR
    return "$dataDir/$relativePath"
}
