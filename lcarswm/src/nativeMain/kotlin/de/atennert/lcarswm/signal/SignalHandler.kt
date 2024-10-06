package de.atennert.lcarswm.signal

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.system.api.PosixApi
import kotlinx.cinterop.*
import platform.posix.SA_NOCLDSTOP
import platform.posix.sigaction
import platform.posix.sigset_t

@ExperimentalForeignApi
private var globalPosixApi: PosixApi? = null

@ExperimentalForeignApi
private fun handleCoreSignal() {
    globalPosixApi?.abort()
}

@ExperimentalForeignApi
class SignalHandler(private val posixApi: PosixApi) {
    private val allSignals: sigset_t = nativeHeap.alloc()

    private val oldActions = mutableMapOf<Signal, sigaction>()

    init {
        globalPosixApi = posixApi
        val action = nativeHeap.alloc<sigaction>()

        posixApi.sigFillSet(allSignals.ptr)
        posixApi.sigEmptySet(action.sa_mask.ptr)

        action.__sigaction_handler.sa_handler = staticCFunction<Int, Unit> { handleCoreSignal() }
        action.sa_flags = SA_NOCLDSTOP

        Signal.CORE_DUMP_SIGNALS
                // don't register for ABRT because it will be triggered by abort from other core signals
            .filter { it != Signal.ABRT }
            .forEach {
                val oldSignal = nativeHeap.alloc<sigaction>()
                posixApi.sigAction(it, action.ptr, oldSignal.ptr)
                oldActions[it] = oldSignal
            }

        closeWith(SignalHandler::cleanup)
    }

    fun addSignalCallback(signal: Signal, signalHandler: CPointer<CFunction<(Int) -> Unit>>) {
        val action = nativeHeap.alloc<sigaction>()
        posixApi.sigEmptySet(action.sa_mask.ptr)
        action.__sigaction_handler.sa_handler = signalHandler
        action.sa_flags = SA_NOCLDSTOP

        val oldSignal = nativeHeap.alloc<sigaction>()
        posixApi.sigAction(signal, action.ptr, oldSignal.ptr)
        oldActions[signal] = oldSignal
    }

    private fun cleanup() {
        oldActions.forEach { (signalValue, action) ->
            posixApi.sigAction(signalValue, action.ptr, null)
        }
        oldActions.clear()
    }
}