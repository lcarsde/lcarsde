package de.atennert.lcarswm.events

import de.atennert.lcarswm.AppMenuMessageHandler
import de.atennert.lcarswm.Environment
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.command.Commander
import de.atennert.lcarswm.keys.*
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemCallMocker
import de.atennert.lcarswm.system.SystemFacadeMock
import de.atennert.lcarswm.window.WindowFocusHandler
import de.atennert.lcarswm.window.WindowList
import kotlinx.cinterop.*
import xlib.*
import kotlin.test.*

/**
 *
 */
@ExperimentalForeignApi
class KeyReleaseHandlerTest : SystemCallMocker() {
    private val keySetting = setOf(
        KeyExecution("Ctrl+F4", "command arg1 arg2"),
        KeyAction("Alt+F4", WmAction.WINDOW_CLOSE),
        KeyAction("Lin+Q", WmAction.WM_QUIT)
    )

    @AfterTest
    override fun teardown() {
        closeClosables()
        super.teardown()
    }

    @Test
    fun `return the event type KeyReleaseHandler`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val keyManager = KeyManager(systemApi)
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, FakeCommander())

        assertEquals(
            KeyRelease,
            keyReleaseHandler.xEventType,
            "The type of the key release handler should be KeyRelease"
        )
    }

    @Test
    fun `shutdown on Q`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val keyManager = KeyManager(systemApi)
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)

        systemApi.functionCalls.clear()

        val keyReleaseEvent = nativeHeap.alloc<XEvent>()
        keyReleaseEvent.type = KeyRelease
        keyReleaseEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Q).convert()
        keyReleaseEvent.xkey.state = getMask(keyManager, listOf(Modifiers.SUPER))

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, FakeCommander())

        val shutdownValue = keyReleaseHandler.handleEvent(keyReleaseEvent)

        assertTrue(shutdownValue, "The window manager should shut down on Q")
    }

    @Test
    fun `kill active window if the WM protocols result is != 0 on F4`() {
        val systemApi = object : SystemFacadeMock() {
            override fun getWMProtocols(
                window: Window,
                protocolsReturn: CPointer<CPointerVar<AtomVar>>,
                protocolCountReturn: CPointer<IntVar>
            ): Int = -1
        }
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val windowId = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)
        focusHandler.setFocusedWindow(windowId)

        val keyReleaseEvent = nativeHeap.alloc<XEvent>()
        keyReleaseEvent.type = KeyRelease
        keyReleaseEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_F4).convert()
        keyReleaseEvent.xkey.state = getMask(keyManager, listOf(Modifiers.ALT))

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, FakeCommander())

        systemApi.functionCalls.clear()

        val shutdownValue = keyReleaseHandler.handleEvent(keyReleaseEvent)

        assertFalse(shutdownValue, "The window manager should not shut down on F4")

        val killClientCall = systemApi.functionCalls.removeAt(0)
        assertEquals("killClient", killClientCall.name, "The window should be killed if we can't get its protocols")
        assertEquals(windowId, killClientCall.parameters[0], "The active window needs to be killed")
    }

    @Test
    fun `kill active window if the WM protocols don't contain WM_DELETE_WINDOW`() {
        val systemApi = object : SystemFacadeMock() {
            override fun getWMProtocols(
                window: Window,
                protocolsReturn: CPointer<CPointerVar<AtomVar>>,
                protocolCountReturn: CPointer<IntVar>
            ): Int {
                protocolCountReturn.pointed.value = 0
                return 0
            }
        }
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val windowId = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)
        focusHandler.setFocusedWindow(windowId)

        val keyReleaseEvent = nativeHeap.alloc<XEvent>()
        keyReleaseEvent.type = KeyRelease
        keyReleaseEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_F4).convert()
        keyReleaseEvent.xkey.state = getMask(keyManager, listOf(Modifiers.ALT))

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, FakeCommander())

        systemApi.functionCalls.clear()

        val shutdownValue = keyReleaseHandler.handleEvent(keyReleaseEvent)

        assertFalse(shutdownValue, "The window manager should not shut down on F4")

        val killClientCall = systemApi.functionCalls.removeAt(0)
        assertEquals("killClient", killClientCall.name, "The window should be killed if we can't get its protocols")
        assertEquals(windowId, killClientCall.parameters[0], "The active window needs to be killed")
    }

    @Test
    fun `send delete window event on F4 when client supports it`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val windowId = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)
        focusHandler.setFocusedWindow(windowId)

        val keyReleaseEvent = nativeHeap.alloc<XEvent>()
        keyReleaseEvent.type = KeyRelease
        keyReleaseEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_F4).convert()
        keyReleaseEvent.xkey.state = getMask(keyManager, listOf(Modifiers.ALT))

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, FakeCommander())

        systemApi.functionCalls.clear()

        val shutdownValue = keyReleaseHandler.handleEvent(keyReleaseEvent)

        assertFalse(shutdownValue, "The window manager should not shut down on F4")

        val xClientCall = systemApi.functionCalls.removeAt(0)
        assertEquals("sendEvent", xClientCall.name, "The WM should send a delete window request")
        assertEquals(windowId, xClientCall.parameters[0], "The delete request should be for the right window")
        assertFalse(xClientCall.parameters[1] as Boolean, "The event should not propagate")
        assertEquals(0.toLong(), xClientCall.parameters[2], "The mask should not contain anything")

        val eventData = (xClientCall.parameters[3] as CPointer<XEvent>).pointed
        assertEquals(ClientMessage, eventData.xclient.type, "The message type should be client data")
        assertEquals(windowId, eventData.xclient.window, "The window data should match")
        assertEquals(atomLibrary[Atoms.WM_PROTOCOLS], eventData.xclient.message_type, "The message type should be WM_PROTOCOLS")
        assertEquals(32, eventData.xclient.format, "The message format should be 32 bit")
        assertEquals(atomLibrary[Atoms.WM_DELETE_WINDOW], eventData.xclient.data.l[0].convert(), "The delete WM window atom needs to match")
    }

    @Test
    fun `execute configured command`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val windowId = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        val atomLibrary = AtomLibrary(systemApi)
        val commander = FakeCommander()
        focusHandler.setFocusedWindow(windowId)

        val keyReleaseHandler = KeyReleaseHandler(LoggerMock(), systemApi, focusHandler, keyManager, keyConfiguration, keySessionManager, atomLibrary, commander)

        systemApi.functionCalls.clear()

        val keyReleaseEvent = nativeHeap.alloc<XEvent>()
        keyReleaseEvent.type = KeyRelease
        keyReleaseEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_F4).convert()
        keyReleaseEvent.xkey.state = getMask(keyManager, listOf(Modifiers.CONTROL))

        val shutdownValue = keyReleaseHandler.handleEvent(keyReleaseEvent)

        assertFalse(shutdownValue, "The window manager should not shut down on Ctrl+F4")

        assertContains(commander.calls, listOf("command", "arg1", "arg2"))
    }

    private class FakeCommander : Commander() {
        val calls = mutableListOf<List<String>>()

        override fun run(command: List<String>): Boolean {
            calls.add(command)
            return true
        }
    }

    private fun getMask(keyManager: KeyManager, l: List<Modifiers>): UInt {
        return l.fold(0) { acc, m ->
            acc or keyManager.modMasks.getValue(m)
        }.convert()
    }
}
