package de.atennert.lcarswm.events

import de.atennert.lcarswm.AppMenuMessageHandler
import de.atennert.lcarswm.Environment
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.keys.*
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.monitor.MonitorManagerMock
import de.atennert.lcarswm.system.SystemCallMocker
import de.atennert.lcarswm.system.SystemFacadeMock
import de.atennert.lcarswm.window.WindowCoordinatorMock
import de.atennert.lcarswm.window.WindowFocusHandler
import de.atennert.lcarswm.window.WindowList
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.*
import kotlin.test.*

@ExperimentalForeignApi
class KeyPressHandlerTest : SystemCallMocker() {

    private val keySetting = setOf(
        KeyAction("Alt+Up", WmAction.WINDOW_MOVE_NEXT),
        KeyAction("Alt+Down", WmAction.WINDOW_MOVE_PREVIOUS),
        KeyAction("Alt+Tab", WmAction.WINDOW_TOGGLE_FWD),
        KeyAction("Alt+Shift+Tab", WmAction.WINDOW_TOGGLE_BWD),
        KeyAction("Lin+M", WmAction.SCREEN_MODE_TOGGLE),
        KeyAction("Lin+Ctrl+Up", WmAction.WINDOW_SPLIT_UP),
        KeyAction("Lin+Ctrl+Down", WmAction.WINDOW_SPLIT_DOWN),
        KeyAction("Lin+Ctrl+Left", WmAction.WINDOW_SPLIT_LEFT),
        KeyAction("Lin+Ctrl+Right", WmAction.WINDOW_SPLIT_RIGHT),
    )

    @AfterTest
    override fun teardown() {
        closeClosables()
        super.teardown()
    }

    @Test
    fun `return the event type KeyPressHandler`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        assertEquals(KeyPress, keyPressHandler.xEventType, "The key press handler should have the correct type")
    }

    @Test
    fun `move active window to the next monitor`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Up).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        val moveWindowCall = windowCoordinator.functionCalls.removeAt(0)
        assertEquals("moveWindowToNextMonitor", moveWindowCall.name, "The focused window should be moved to the next monitor")
        assertEquals(windowFocusHandler.getFocusedWindow(), moveWindowCall.parameters[0], "The _focused window_ should be moved to the next monitor")
    }

    @Test
    fun `move active window to the previous monitor`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Down).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        val moveWindowCall = windowCoordinator.functionCalls.removeAt(0)
        assertEquals("moveWindowToPreviousMonitor", moveWindowCall.name, "The focused window should be moved to the previous monitor")
        assertEquals(windowFocusHandler.getFocusedWindow(), moveWindowCall.parameters[0], "The _focused window_ should be moved to the previous monitor")
    }

    @Test
    fun `don't react on move to next monitor without a focusable window`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Up).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertTrue(windowCoordinator.functionCalls.isEmpty(), "There should be no call to the window coordinator without focused window")
   }

    @Test
    fun `don't react on move to previous monitor without a focusable window`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Down).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertTrue(windowCoordinator.functionCalls.isEmpty(), "There should be no call to the window coordinator without focused window")
    }

    @Test
    fun `toggle focused Window forwards`() {
        val systemApi = object : SystemFacadeMock() {
            override fun keysymToKeycode(keySym: KeySym): KeyCode {
                if (keySym.convert<Int>() == XK_Tab) {
                    return 42.convert()
                }
                return super.keysymToKeycode(keySym)
            }
        }
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val window1 = systemApi.getNewWindowId()
        val window2 = systemApi.getNewWindowId()
        val window3 = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(window1)
        windowFocusHandler.setFocusedWindow(window2)
        windowFocusHandler.setFocusedWindow(window3)
        windowFocusHandler.setFocusedWindow(window1)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = 42.convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertEquals(window3, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 1")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window2, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 2")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window1, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 3")
    }

    @Test
    fun `toggle focused window backwards`() {
        val systemApi = object : SystemFacadeMock() {
            override fun keysymToKeycode(keySym: KeySym): KeyCode {
                if (keySym.convert<Int>() == XK_Tab) {
                    return 42.convert()
                }
                return super.keysymToKeycode(keySym)
            }
        }
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val window1 = systemApi.getNewWindowId()
        val window2 = systemApi.getNewWindowId()
        val window3 = systemApi.getNewWindowId()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(window1)
        windowFocusHandler.setFocusedWindow(window2)
        windowFocusHandler.setFocusedWindow(window3)
        windowFocusHandler.setFocusedWindow(window1)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = 42.convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT)
            .or(keyManager.modMasks.getValue(Modifiers.SHIFT)).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertEquals(window2, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 1")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window3, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 2")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window1, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated 3")
    }

    @Test
    fun `don't toggle without focusable windows`() {
        val systemApi = object : SystemFacadeMock() {
            override fun keysymToKeycode(keySym: KeySym): KeyCode {
                if (keySym.convert<Int>() == XK_Tab) {
                    return 42.convert()
                }
                return super.keysymToKeycode(keySym)
            }
        }
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = 42.convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertEquals(0, windowCoordinator.functionCalls.size, "There is no window to restack")
    }

    @Test
    fun `toggle screen mode on M`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_M).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the M-key shouldn't trigger a shutdown.")

        val monitorModeToggleCall = monitorManager.functionCalls.removeAt(0)
        assertEquals("toggleScreenMode", monitorModeToggleCall.name, "The screen mode should be toggled.")
    }

    @Test
    fun `split window up`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Up).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER)
            .or(keyManager.modMasks.getValue(Modifiers.SHIFT)).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling window split up shouldn't trigger a shutdown")

    }

    @Test
    fun `split window down`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Down).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER)
            .or(keyManager.modMasks.getValue(Modifiers.SHIFT)).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling window split down shouldn't trigger a shutdown")

    }

    @Test
    fun `split window left`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Left).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER)
            .or(keyManager.modMasks.getValue(Modifiers.SHIFT)).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling window split left shouldn't trigger a shutdown")

    }

    @Test
    fun `split window right`() {
        val systemApi = SystemFacadeMock()
        val env = Environment(systemApi.display)
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val monitorManager = MonitorManagerMock()
        val keySessionManager = KeySessionManager(LoggerMock(), env)
        val keyConfiguration = KeyConfiguration(
            systemApi,
            keySetting,
            keyManager,
            keySessionManager,
            systemApi.rootWindowId
        )
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, keyConfiguration, keySessionManager, monitorManager, windowCoordinator, windowFocusHandler)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySymKeyCodeMapping.getValue(XK_Right).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER)
            .or(keyManager.modMasks.getValue(Modifiers.SHIFT)).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling window split right shouldn't trigger a shutdown")

    }
}
