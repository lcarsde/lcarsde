package de.atennert.lcarswm.monitor

import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

actual fun <Output> getOutputHash(output: Output): Int {
    @OptIn(ExperimentalForeignApi::class)
    if (output is RROutput) {
        return output.toInt()
    }
    throw UnsupportedOperationException("getOutputHash is not defined for the type of $output")
}