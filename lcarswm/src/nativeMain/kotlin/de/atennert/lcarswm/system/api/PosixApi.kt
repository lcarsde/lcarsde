package de.atennert.lcarswm.system.api

import de.atennert.lcarswm.signal.Signal
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.sigaction
import platform.posix.sigset_t

/**
 * Interface for accessing POSIX functions
 */
@ExperimentalForeignApi
interface PosixApi {
    fun usleep(time: UInt)

    fun abort()

    fun sigFillSet(sigset: CPointer<sigset_t>)

    fun sigEmptySet(sigset: CPointer<sigset_t>)

    fun sigAction(signal: Signal, newSigAction: CPointer<sigaction>, oldSigAction: CPointer<sigaction>?)
}