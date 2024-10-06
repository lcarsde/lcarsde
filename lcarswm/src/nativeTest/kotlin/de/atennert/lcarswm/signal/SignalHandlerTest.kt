package de.atennert.lcarswm.signal

import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalForeignApi
class SignalHandlerTest {
    @BeforeTest
    fun setup() {
        receivedSignal = null
    }

    @Test
    fun `handle core signal`() {
        val system = SystemFacadeMock()
        SignalHandler(system)

        system.functionCalls.clear()

        system.signalActions
            .onEach { (signal, actionPtr) -> actionPtr.pointed.__sigaction_handler.sa_handler?.invoke(signal.signalValue) }
            .forEach { (signal, _) ->
                val abortCall = system.functionCalls.removeAt(0)
                assertEquals("abort", abortCall.name, "Call abort for signal $signal")
            }
    }

    @Test
    fun `add signal callback`() {
        val system = SystemFacadeMock()
        val signalHandler = SignalHandler(system)

        system.functionCalls.clear()

        signalHandler.addSignalCallback(Signal.INT, staticCFunction<Int, Unit> { receivedSignal = it })

        system.signalActions[Signal.INT]?.pointed?.__sigaction_handler?.sa_handler?.invoke(Signal.INT.signalValue)

        assertEquals(Signal.INT.signalValue, receivedSignal, "We should get the signal, that we registered for")
    }
}

private var receivedSignal: Int? = null
