package de.atennert.lcarswm.window

import de.atennert.lcarswm.AppMenuMessageHandler
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.Window
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalForeignApi
class WindowFocusHandlerTest {
    @AfterTest
    fun teardown() {
        closeClosables()
    }

    @Test
    fun `check that initially there is no focused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        assertNull(windowFocusHandler.getFocusedWindow(), "There is no focused window")

        var activeWindow: Window? = 42.convert()
        var oldWindow: Window? = 64.convert()
        windowFocusHandler.registerObserver { n, o, _ -> activeWindow = n; oldWindow = o}
        assertNull(activeWindow, "The observer should get null window")
        assertNull(oldWindow, "The observer should get no old window")
    }

    @Test
    fun `update focused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val windowFocusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val testWindow1 = 21.convert<Window>()
        val testWindow2 = 22.convert<Window>()
        var newWindow: Window? = 42.convert()
        var oldWindow: Window? = 64.convert()

        windowFocusHandler.registerObserver { n, o, _ -> newWindow = n; oldWindow = o}

        windowFocusHandler.setFocusedWindow(testWindow1)
        assertEquals(testWindow1, windowFocusHandler.getFocusedWindow(), "The focused window should be updated (1)")
        assertEquals(testWindow1, newWindow, "The observer should get the updated window (1)")
        assertNull(oldWindow, "The observer should get the old window (1)")

        windowFocusHandler.setFocusedWindow(testWindow2)
        assertEquals(testWindow2, windowFocusHandler.getFocusedWindow(), "The focused window should be updated (2)")
        assertEquals(testWindow2, newWindow, "The observer should get the updated window (2)")
        assertEquals(testWindow1, oldWindow, "The observer should get the old window (2)")
    }

    @Test
    fun `remove last focused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        windowList.add(FakeManagedWindow(id = 1.convert()))

        windowList.remove(1.toULong())

        assertNull(focusHandler.getFocusedWindow(), "The focused window should be removed")
    }

    @Test
    fun `remove second focused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        windowList.add(FakeManagedWindow(id = 1.convert()))
        windowList.add(FakeManagedWindow(id = 2.convert()))

        windowList.remove(2.toULong())


        assertEquals(1.convert(), focusHandler.getFocusedWindow(), "The fallback should be another focusable window")
    }

    @Test
    fun `remove third focused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        windowList.add(FakeManagedWindow(id = 1.convert()))
        windowList.add(FakeManagedWindow(id = 2.convert()))
        windowList.add(FakeManagedWindow(id = 3.convert()))

        // remove currently focused window
        windowList.remove(3.toULong())

        // the last focused window should be focused
        assertEquals(2.convert(), focusHandler.getFocusedWindow(), "The fallback should be the last focused window")
    }

    @Test
    fun `remove unfocused window`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        windowList.add(FakeManagedWindow(id = 1.convert()))
        windowList.add(FakeManagedWindow(id = 2.convert()))
        windowList.add(FakeManagedWindow(id = 3.convert()))

        windowList.remove(2.toULong())

        assertEquals(3.convert(), focusHandler.getFocusedWindow(), "Don't unfocus the focused window")
    }

    @Test
    fun `toggle through windows`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)

        focusHandler.toggleWindowFocusForward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.toggleWindowFocusForward()
        assertEquals(window1, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.toggleWindowFocusForward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")
    }

    @Test
    fun `toggle through windows with reset`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)

        focusHandler.toggleWindowFocusForward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusForward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusForward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle back to window 2")
    }

    @Test
    fun `toggle through windows reversed`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window1, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")
    }

    @Test
    fun `toggle through windows reversed with reset`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window1, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusBackward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusBackward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle back to window 3")
    }

    @Test
    fun `toggle through windows mixed`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()
        val window4: Window = 4.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)
        focusHandler.setFocusedWindow(window4)

        focusHandler.toggleWindowFocusForward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")

        focusHandler.toggleWindowFocusForward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window4, focusHandler.getFocusedWindow(), "The focus should toggle to window 4")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window1, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.toggleWindowFocusForward()
        assertEquals(window4, focusHandler.getFocusedWindow(), "The focus should toggle to window 4")
    }

    @Test
    fun `toggle through windows mixed with reset`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))
        val window1: Window = 1.convert()
        val window2: Window = 2.convert()
        val window3: Window = 3.convert()
        val window4: Window = 4.convert()

        focusHandler.setFocusedWindow(window1)
        focusHandler.setFocusedWindow(window2)
        focusHandler.setFocusedWindow(window3)
        focusHandler.setFocusedWindow(window4)

        focusHandler.toggleWindowFocusForward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle to window 3")

        focusHandler.toggleWindowFocusForward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusForward()
        assertEquals(window4, focusHandler.getFocusedWindow(), "The focus should toggle to window 2")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window2, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.toggleWindowFocusBackward()
        assertEquals(window1, focusHandler.getFocusedWindow(), "The focus should toggle to window 1")

        focusHandler.keySessionListener.stopSession()
        focusHandler.toggleWindowFocusBackward()
        assertEquals(window3, focusHandler.getFocusedWindow(), "The focus should toggle back to window 3")
    }

    @Test
    fun `don't react on toggle without windows`() {
        val systemApi = SystemFacadeMock()
        val windowList = WindowList()
        val focusHandler = WindowFocusHandler(windowList, AppMenuMessageHandler(LoggerMock(), systemApi, AtomLibrary(systemApi), windowList))

        focusHandler.toggleWindowFocusForward()

        assertNull(focusHandler.getFocusedWindow(), "There is no focusable window")
    }
}
