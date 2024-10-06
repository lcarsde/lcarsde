package de.atennert.lcarswm.window

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.conversion.toULongArray
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.system.WindowUtilApiMock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.Atom
import xlib.Window
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalForeignApi
class UpdateWindowListAtomTest {

    private val windowUtilApi = object : WindowUtilApiMock() {
        var windows = ubyteArrayOf()
            private set

        fun reset() {
            windows = ubyteArrayOf()
        }

        override fun changeProperty(
            window: Window,
            propertyAtom: Atom,
            typeAtom: Atom,
            data: UByteArray?,
            format: Int,
            mode: Int
        ): Int {
            data?.let { windows = it }
            return 1
        }

        override fun internAtom(name: String, onlyIfExists: Boolean): Atom {
            return 2.convert()
        }
    }

    @BeforeTest
    fun setup() {
        windowUtilApi.reset()
    }

    @AfterTest
    fun tearDown() {
        closeClosables()
    }

    @Test
    fun `add one window to list`() {
        val windowList = WindowList()
        updateWindowListAtom(1.convert(), windowUtilApi, AtomLibrary(windowUtilApi), windowList)

        windowList.add(FakeManagedWindow(id = 42.convert()))

        assertEquals(42.toULong(), windowUtilApi.windows.toULongArray()[0])
    }

    @Test
    fun `add two windows to list`() {
        val windowList = WindowList()
        updateWindowListAtom(1.convert(), windowUtilApi, AtomLibrary(windowUtilApi), windowList)

        windowList.add(FakeManagedWindow(id = 42.convert()))
        windowList.add(FakeManagedWindow(id = 21.convert()))

        assertEquals(8, windowUtilApi.windows.size)
        assertEquals(42.toULong(), windowUtilApi.windows.toULongArray()[0])
        assertEquals(21.toULong(), windowUtilApi.windows.toULongArray()[1])
    }

    @Test
    fun `remove window from list`() {
        val windowList = WindowList()
        updateWindowListAtom(1.convert(), windowUtilApi, AtomLibrary(windowUtilApi), windowList)

        windowList.add(FakeManagedWindow(id = 42.convert()))
        windowList.add(FakeManagedWindow(id = 21.convert()))

        windowList.remove(42.toULong())

        assertEquals(4, windowUtilApi.windows.size)
        assertEquals(21.toULong(), windowUtilApi.windows.toULongArray()[0])
    }

    @Test
    fun `remove last window from list`() {
        val windowList = WindowList()
        updateWindowListAtom(1.convert(), windowUtilApi, AtomLibrary(windowUtilApi), windowList)

        windowList.add(FakeManagedWindow(id = 42.convert()))

        windowList.remove(42.toULong())

        assertEquals(0, windowUtilApi.windows.size)
    }
}