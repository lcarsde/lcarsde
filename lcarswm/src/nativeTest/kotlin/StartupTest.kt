import de.atennert.lcarswm.ResourceGenerator
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.*
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.signal.Signal
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import kotlinx.coroutines.runBlocking
import xlib.*
import kotlin.collections.set
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@ExperimentalForeignApi
@ExperimentalNativeApi
class StartupTest {
    private class FakeResourceGenerator : ResourceGenerator {
        val variables = mutableMapOf<String, String>()

        override fun createEnvironment(): Environment {
            return object : Environment {
                override fun get(name: String): String? = null
                override fun set(name: String, value: String): Boolean {
                    variables[name] = value
                    return true
                }
            }
        }

        override fun createFiles(): Files {
            return object : Files {
                override fun exists(path: String): Boolean = true

                override fun readLines(path: String, consumer: (String) -> Unit) {}
            }
        }

        override fun createFileFactory(): FileFactory {
            return object : FileFactory {
                override fun getDirectory(path: String): Directory? = null

                override fun getFile(path: String, accessMode: AccessMode): File {
                    throw NotImplementedError()
                }
            }
        }
    }

    @Test
    fun `check startup`() = runBlocking {
        val systemFacade = StartupFacadeMock()
        val resourceGenerator = FakeResourceGenerator()

        runWindowManager(systemFacade, LoggerMock(), resourceGenerator)

        val startupCalls = systemFacade.functionCalls

        checkCoreSignalRegistration(startupCalls)

        assertEquals("openDisplay", startupCalls.removeAt(0).name, "try to open the display")

        checkForAtomRegistration(startupCalls)

        checkUserSignalRegistration(startupCalls)

        assertEquals("defaultScreenOfDisplay", startupCalls.removeAt(0).name, "try to get the displays screen")

        assertEquals("synchronize", startupCalls.removeAt(0).name, "synchronize after getting the display and randr resources")

        assertEquals(systemFacade.displayString, resourceGenerator.variables["DISPLAY"])

        checkCreatingTheSupportWindow(startupCalls, systemFacade)

        checkBecomeScreenOwner(startupCalls)
    }


    private fun checkCoreSignalRegistration(startupCalls: MutableList<FunctionCall>) {
        assertEquals("sigFillSet", startupCalls.removeAt(0).name, "Initialize the signal set")
        assertEquals("sigEmptySet", startupCalls.removeAt(0).name, "Initialize the action signal set")

        Signal.CORE_DUMP_SIGNALS
            .filter { it != Signal.ABRT }
            .forEach {
                val signalRegistrationCall = startupCalls.removeAt(0)
                assertEquals("sigAction", signalRegistrationCall.name, "Initialize signal $it")
                assertEquals(it, signalRegistrationCall.parameters[0], "Initialize signal _${it}_")
            }
    }

    private fun checkForAtomRegistration(startupCalls: MutableList<FunctionCall>) {
        Atoms.values().forEach { atom ->
            val registrationCall = startupCalls.removeAt(0)
            assertEquals("internAtom", registrationCall.name, "$atom needs to be registered")
            assertEquals(atom.atomName, registrationCall.parameters[0], "_${atom}_ needs to be registered")
        }
    }

    private fun checkUserSignalRegistration(startupCalls: MutableList<FunctionCall>) {
        listOf(Signal.USR1, Signal.USR2, Signal.TERM, Signal.INT, Signal.HUP, Signal.PIPE, Signal.CHLD, Signal.TTIN, Signal.TTOU)
            .forEach {
                assertEquals("sigEmptySet", startupCalls.removeAt(0).name, "Initialize the action signal set")
                val signalRegistrationCall = startupCalls.removeAt(0)
                assertEquals("sigAction", signalRegistrationCall.name, "Initialize signal $it")
                assertEquals(it, signalRegistrationCall.parameters[0], "Initialize signal _${it}_")
            }
    }

    private fun checkCreatingTheSupportWindow(startupCalls: MutableList<FunctionCall>, system: SystemFacadeMock) {
        val createSupportWindowCall = startupCalls.removeAt(0)
        val mapSupportWindowCall = startupCalls.removeAt(0)
        val lowerSupportWindowCall = startupCalls.removeAt(0)

        assertEquals("createWindow", createSupportWindowCall.name, "Create the EWHM support window")
        assertEquals(system.rootWindowId, createSupportWindowCall.parameters[0], "Create the EWHM support window as parent of root")

        assertEquals("mapWindow", mapSupportWindowCall.name, "Map the EWHM support window")
        assertEquals((system.rootWindowId + 1.convert<Window>()), mapSupportWindowCall.parameters[0], "Map the _EWHM support window_")

        assertEquals("lowerWindow", lowerSupportWindowCall.name, "Lower the EWHM support window")
        assertEquals((system.rootWindowId + 1.convert<Window>()), lowerSupportWindowCall.parameters[0], "Lower the _EWHM support window_")
    }

    private fun checkBecomeScreenOwner(startupCalls: MutableList<FunctionCall>) {
        val wmsnAtomCall = startupCalls.removeAt(0)
        assertEquals("internAtom", wmsnAtomCall.name, "WMSN needs to be registered")
        assertEquals("WM_S42", wmsnAtomCall.parameters[0], "_WMSN_ needs to be registered")
    }

    @Test
    fun `send client message informing that we are the WM`() = runBlocking {
        val systemFacade = StartupFacadeMock()
        val resourceGenerator = FakeResourceGenerator()

        runWindowManager(systemFacade, LoggerMock(), resourceGenerator)

        val startupCalls = systemFacade.functionCalls.takeWhile { it.name != "nextEvent" }

        val sendEventCall = startupCalls.singleOrNull { it.name == "sendEvent" && it.parameters[0] == systemFacade.rootWindowId }

        assertNotNull(sendEventCall, "We need to send an event to notify about lcarswm being the WM")

        assertFalse(sendEventCall.parameters[1] as Boolean, "Don't propagate")

        assertEquals(SubstructureNotifyMask, sendEventCall.parameters[2], "The event mask is substructure notify")

        val eventData = sendEventCall.parameters[3] as CPointer<XEvent>
        assertEquals(ClientMessage, eventData.pointed.xclient.type, "The event needs to be a client message")
    }

    @Test
    fun `set required properties`() = runBlocking {
        val systemFacade = StartupFacadeMock()
        val rootWindow: Window = 1.convert() // hard coded in SystemFacadeMock
        val supportWindow: Window = systemFacade.nextWindowId // first created window starts at 2 in SystemFacadeMock
        val resourceGenerator = FakeResourceGenerator()

        runWindowManager(systemFacade, LoggerMock(), resourceGenerator)

        val propertyCalls = systemFacade.functionCalls.filter { it.name == "changeProperty" }
        val atoms = systemFacade.atomMap

        val expectedProperties = listOf(
            Pair(rootWindow, "_NET_SUPPORTING_WM_CHECK"),
            Pair(rootWindow, "_NET_SUPPORTED"),
            Pair(supportWindow, "_NET_SUPPORTING_WM_CHECK"),
            Pair(supportWindow, "_NET_WM_NAME")
        )

        expectedProperties.forEach { (window, atomName) ->
            assertNotNull(
                propertyCalls.find { it.parameters[0] == window && it.parameters[1] == atoms[atomName] },
                "The property $atomName should set/changed on window $window"
            )
        }

    }

    private class StartupFacadeMock : SystemFacadeMock() {
        override fun getQueuedEvents(mode: Int): Int {
            return 1
        }

        var eventCount = 1
        override fun nextEvent(event: CPointer<XEvent>): Int {
            when (eventCount) {
                1, 2 -> {
                    event.pointed.type = PropertyNotify
                    event.pointed.xproperty.time = eventCount.convert()
                }
                else -> {
                    super.nextEvent(event)
                    event.pointed.type = KeyRelease
                    event.pointed.xkey.time = 234.convert()
                    event.pointed.xkey.keycode = keySymKeyCodeMapping.getValue(XK_Q).convert()
                    event.pointed.xkey.state = 0x40.convert()
                }
            }
            eventCount++
            return Success
        }
    }
}