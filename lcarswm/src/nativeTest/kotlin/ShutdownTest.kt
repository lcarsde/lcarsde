import de.atennert.lcarswm.HOME_CACHE_DIR_PROPERTY
import de.atennert.lcarswm.HOME_CONFIG_DIR_PROPERTY
import de.atennert.lcarswm.ResourceGenerator
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.*
import de.atennert.lcarswm.lifecycle.ROOT_WINDOW_MASK
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.signal.Signal
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.lcarswm.system.SystemCallMocker
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import xlib.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalNativeApi
@ExperimentalForeignApi
class ShutdownTest : SystemCallMocker() {
    private class FakeResourceGenerator : ResourceGenerator {
        override fun createEnvironment(): Environment {
            return object : Environment {
                override fun get(name: String): String = when(name) {
                    HOME_CONFIG_DIR_PROPERTY -> "/home/me"
                    HOME_CACHE_DIR_PROPERTY -> "/home/me"
                    else -> error("getenv with unsimulated key: $name")
                }
                override fun set(name: String, value: String): Boolean = true
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
    fun `shutdown when there's no display to get`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        val testFacade = object : SystemFacadeMock() {
            override fun openDisplay(): Boolean {
                super.openDisplay()
                return false
            }
        }
        val resourceGenerator = FakeResourceGenerator()

        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "openDisplay" }
            .drop(1)
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkSynchronizationRequest(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown when there's no default screen to get`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        val resourceGenerator = FakeResourceGenerator()
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val testFacade = object : SystemFacadeMock() {
            override fun defaultScreenOfDisplay(): CPointer<Screen>? {
                super.defaultScreenOfDisplay()
                return null
            }

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }
        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "defaultScreenOfDisplay" }
            .drop(1)
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkSynchronizationRequest(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown when the old WM doesn't`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val resourceGenerator = FakeResourceGenerator()
        val testFacade = object : SystemFacadeMock() {
            override fun defaultScreenNumber(): Int = 23

            var newSelectionOwner: Window = 123456.convert()
            override fun setSelectionOwner(atom: Atom, window: Window, time: Time): Int {
                super.setSelectionOwner(atom, window, time)
                newSelectionOwner = window
                return 0
            }

            override fun getSelectionOwner(atom: Atom): Window {
                super.getSelectionOwner(atom)
                return newSelectionOwner
            }

            var used = false
            override fun getQueuedEvents(mode: Int): Int {
                return if (used) {
                    0
                } else {
                    used = true
                    1
                }
            }

            override fun nextEvent(event: CPointer<XEvent>): Int {
                event.pointed.type = PropertyNotify
                event.pointed.xproperty.time = 123.convert()
                return Success
            }

            override fun usleep(time: UInt) {}

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }

        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
                .dropWhile { it.name != "setSelectionOwner" }
                .drop(1)
                .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkRequestForCurrentSelectionOwner(functionCalls)
        checkSynchronizationRequest(functionCalls)
        checkThatSupportWindowWasDestroyed(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown when the wm can not become screen owner`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val resourceGenerator = FakeResourceGenerator()
        val testFacade = object : SystemFacadeMock() {
            var selectionOwnerCounter = 0

            override fun defaultScreenNumber(): Int = 23

            override fun getSelectionOwner(atom: Atom): Window {
                return if (atom == atomMap["WM_S${defaultScreenNumber()}"]) {
                    super.functionCalls.add(FunctionCall("getSelectionOwner", atom, selectionOwnerCounter++))
                    None.convert()
                } else {
                    super.getSelectionOwner(atom)
                }
            }

            var used = false
            override fun getQueuedEvents(mode: Int): Int {
                return if (used) {
                    0
                } else {
                    used = true
                    1
                }
            }

            override fun nextEvent(event: CPointer<XEvent>): Int {
                event.pointed.type = PropertyNotify
                event.pointed.xproperty.time = 123.convert()
                return Success
            }

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }

        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "getSelectionOwner" || it.parameters.elementAtOrNull(1) != 1 }
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkRequestForCurrentSelectionOwner(functionCalls)
        checkSynchronizationRequest(functionCalls)
        checkThatSupportWindowWasDestroyed(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown when there is an error response for select input`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val resourceGenerator = FakeResourceGenerator()
        val testFacade = object : SystemFacadeMock() {
            private lateinit var errorHandler: XErrorHandler

            override fun setErrorHandler(handler: XErrorHandler): XErrorHandler? {
                this.errorHandler = handler
                return super.setErrorHandler(handler)
            }

            override fun selectInput(window: Window, mask: Long): Int {
                this.errorHandler.invoke(null, null)
                return super.selectInput(window, mask)
            }

            var used = false
            override fun getQueuedEvents(mode: Int): Int {
                return if (used) {
                    0
                } else {
                    used = true
                    1
                }
            }

            override fun nextEvent(event: CPointer<XEvent>): Int {
                event.pointed.type = PropertyNotify
                event.pointed.xproperty.time = 123.convert()
                return Success
            }

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }
        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "setErrorHandler" }
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkSettingOfErrorHandler(functionCalls)
        checkSelectInputSetting(functionCalls, ROOT_WINDOW_MASK)
        checkSynchronizationRequest(functionCalls)
        checkSynchronizationRequest(functionCalls)
        checkThatSupportWindowWasDestroyed(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown after sending shutdown key combo`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val resourceGenerator = FakeResourceGenerator()
        val testFacade = object : SystemFacadeMock() {
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

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }

        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "nextEvent" }
            .dropWhile { it.name == "nextEvent" }
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkFinalizingSync(functionCalls)
        checkClosingOfAppMenuMessageQueues(functionCalls)
        checkFreeingOfColors(functionCalls)
        checkFreeingOfGraphicsContexts(functionCalls)
        checkFreeingOfColorMap(functionCalls)
        checkWindowPropertyRemoval(functionCalls, testFacade.atomMap, "_NET_SUPPORTED")
        checkSelectInputSetting(functionCalls, NoEventMask)
        checkThatSupportWindowWasDestroyed(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    @Test
    fun `shutdown after sending selection clear event`() = runBlocking {
        val logger = LoggerMock().closeWith(LoggerMock::close)
        lateinit var modifierKeymapRef: CPointer<XModifierKeymap>
        lateinit var keymapRef: CPointer<KeySymVar>
        val resourceGenerator = FakeResourceGenerator()
        val testFacade = object : SystemFacadeMock() {

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
                        event.pointed.type = SelectionClear
                    }
                }
                eventCount++
                return Success
            }

            override fun getModifierMapping(): CPointer<XModifierKeymap> {
                modifierKeymapRef = super.getModifierMapping()!!
                return modifierKeymapRef
            }

            override fun getKeyboardMapping(
                firstKeyCode: KeyCode,
                keyCodeCount: Int,
                keySymsPerKeyCode: CPointer<IntVar>
            ): CPointer<KeySymVar> {
                keymapRef = super.getKeyboardMapping(firstKeyCode, keyCodeCount, keySymsPerKeyCode)!!
                return keymapRef
            }
        }

        runWindowManager(testFacade, logger, resourceGenerator)

        val functionCalls = testFacade.functionCalls
            .dropWhile { it.name != "nextEvent" }
            .dropWhile { it.name == "nextEvent" }
            .toMutableList()

        checkThatTheLoggerIsClosed(logger)
        checkFinalizingSync(functionCalls)
        checkClosingOfAppMenuMessageQueues(functionCalls)
        checkFreeingOfColors(functionCalls)
        checkFreeingOfGraphicsContexts(functionCalls)
        checkFreeingOfColorMap(functionCalls)
        checkWindowPropertyRemoval(functionCalls, testFacade.atomMap, "_NET_SUPPORTED")
        checkSelectInputSetting(functionCalls, NoEventMask)
        checkThatSupportWindowWasDestroyed(functionCalls)
        checkThatKeyBindingsWereFreed(functionCalls, modifierKeymapRef, keymapRef)
        checkThatTheDisplayWasClosed(functionCalls)
        checkCleanupOfSignals(functionCalls, testFacade.signalActions.keys)

        checkThatThereIsNoUnexpectedInteraction(functionCalls)
    }

    private fun checkThatTheLoggerIsClosed(logger: LoggerMock) {
        assertTrue(logger.closed, "The logger needs to be closed")
    }

    private fun checkCleanupOfSignals(functionCalls: MutableList<FunctionCall>, registeredSignals: Set<Signal>) {
        repeat(registeredSignals.size) {
            assertEquals("sigAction", functionCalls.removeAt(0).name, "Unregister signal")
        }
    }

    private fun checkFinalizingSync(functionCalls: MutableList<FunctionCall>) {
        assertEquals("sync", functionCalls.removeAt(0).name, "Call sync before cleaning up")
    }

    private fun checkRequestForCurrentSelectionOwner(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "getSelectionOwner",
            functionCalls.removeAt(0).name,
            "should call getSelectionOwner to check if other WM is active"
        )
    }

    private fun checkSettingOfErrorHandler(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "setErrorHandler",
            functionCalls.removeAt(0).name,
            "startup should set an error handler to get notified if another WM is already active (selected the input on the root window)"
        )
    }

    private fun checkSelectInputSetting(
        functionCalls: MutableList<FunctionCall>,
        mask: Long
    ) {
        val selectInputCall = functionCalls.removeAt(0)
        assertEquals(
            "selectInput",
            selectInputCall.name,
            "selectInput needs to be called"
        )
        assertEquals(
            mask,
            selectInputCall.parameters[1],
            "selectInput should be called with the mask $mask"
        )
    }

    private fun checkWindowPropertyRemoval(
        functionCalls: MutableList<FunctionCall>,
        atomMap: Map<String, Atom>,
        propertyName: String
    ) {
        val deletePropertyCall = functionCalls.removeAt(0)
        assertEquals(
            "deleteProperty",
            deletePropertyCall.name,
            "We need to call deleteProperty to unset $propertyName"
        )
        assertEquals(
            atomMap[propertyName],
            deletePropertyCall.parameters[1],
            "The property to delete is $propertyName"
        )
    }

    private fun checkSynchronizationRequest(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "sync",
            functionCalls.removeAt(0).name,
            "startup should sync after select input to get notified for other WMs"
        )
    }

    private fun checkClosingOfAppMenuMessageQueues(functionCalls: MutableList<FunctionCall>) {
        // TODO uses wrappers now
//        // send info queue
//        assertEquals("mqClose", functionCalls.removeAt(0).name, "the app menu message queue needs to be closed on shutdown")
//        assertEquals("mqUnlink", functionCalls.removeAt(0).name, "the app menu message queue needs to be unlinked on shutdown")
//
//        // receive info queue
//        assertEquals("mqClose", functionCalls.removeAt(0).name, "the app menu message queue needs to be closed on shutdown")
//        assertEquals("mqUnlink", functionCalls.removeAt(0).name, "the app menu message queue needs to be unlinked on shutdown")
    }

    private fun checkFreeingOfColors(functionCalls: MutableList<FunctionCall>) {
        assertEquals("freeColors", functionCalls.removeAt(0).name, "the acquired colors need to be freed on shutdown")
    }

    private fun checkFreeingOfColorMap(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "freeColormap",
            functionCalls.removeAt(0).name,
            "the acquired color map needs to be freed on shutdown"
        )
    }

    private fun checkFreeingOfGraphicsContexts(functionCalls: MutableList<FunctionCall>) {
        println("gcs: ${functionCalls.takeWhile { it.name == "freeGC" }.count()}")
        repeat(100) {
            assertEquals(
                "freeGC",
                functionCalls.removeAt(0).name,
                "the acquired graphics contexts needs to be freed on shutdown"
            )
        }
    }

    private fun checkThatKeyBindingsWereFreed(
        functionCalls: MutableList<FunctionCall>,
        modifierKeymapRef: CPointer<XModifierKeymap>,
        keymapRef1: CPointer<KeySymVar>
    ) {
        val modifierMapCall = functionCalls.removeAt(0)
        assertEquals("freeModifiermap", modifierMapCall.name, "The acquired modifier map needs to be freed")
        assertEquals(modifierKeymapRef, modifierMapCall.parameters[0], "The _acquired_ modifier map needs to be freed")

        val keyMapCall = functionCalls.removeAt(0)
        assertEquals("xFree", keyMapCall.name, "The acquired keymap needs to be freed")
        assertEquals(keymapRef1, keyMapCall.parameters[0], "The _acquired_ keymap needs to be freed")
    }

    private fun checkThatSupportWindowWasDestroyed(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "destroyWindow",
            functionCalls.removeAt(0).name,
            "net wm support window needs to be destroyed"
        )
    }

    private fun checkThatTheDisplayWasClosed(functionCalls: MutableList<FunctionCall>) {
        assertEquals(
            "closeDisplay",
            functionCalls.removeAt(0).name,
            "the display needs to be closed when shutting down due to no default screen"
        )
    }

    private fun checkThatThereIsNoUnexpectedInteraction(functionCalls: MutableList<FunctionCall>) {
        assertTrue(
            functionCalls.isEmpty(),
            "There should be no more calls to the system"
        )
    }
}