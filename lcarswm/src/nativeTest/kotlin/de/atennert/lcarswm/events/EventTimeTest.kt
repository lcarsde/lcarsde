package de.atennert.lcarswm.events

import de.atennert.lcarswm.RootWindowPropertyHandler
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import kotlinx.cinterop.*
import xlib.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalForeignApi
class EventTimeTest {
    @Test
    fun `set and get event`() {
        val system = SystemFacadeMock()
        val atomLibrary = AtomLibrary(system)
        val eventBuffer = EventBuffer(system)
        val rootWindowPropertyHandler = RootWindowPropertyHandler(LoggerMock(), system, system.rootWindowId, atomLibrary, eventBuffer)

        val event = nativeHeap.alloc<XEvent>()
        event.type = ButtonPress
        event.xbutton.time = 123.convert()

        val eventTime = EventTime(system, eventBuffer, atomLibrary, rootWindowPropertyHandler)

        eventTime.setTimeFromEvent(event.ptr)

        assertEquals(event.xbutton.time, eventTime.lastEventTime, "The event time should match with the time of the last event")
    }

    @Test
    fun `get event time via buffer`() {
        val system = object : SystemFacadeMock() {
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
        }
        val atomLibrary = AtomLibrary(system)
        val eventBuffer = EventBuffer(system)
        val rootWindowPropertyHandler = RootWindowPropertyHandler(LoggerMock(), system, system.rootWindowId, atomLibrary, eventBuffer)

        val eventTime = EventTime(system, eventBuffer, atomLibrary, rootWindowPropertyHandler)

        system.functionCalls.clear()

        assertEquals(123.convert(), eventTime.lastEventTime, "The event time should match with the time of the available event")

        val changePropertyCall = system.functionCalls.removeAt(0)
        assertEquals("changeProperty", changePropertyCall.name, "Change property needs to be called to trigger getting a time")
        assertEquals(rootWindowPropertyHandler.ewmhSupportWindow, changePropertyCall.parameters[0], "Change the property for the EWMH support window")
        assertEquals(atomLibrary[Atoms.WM_CLASS], changePropertyCall.parameters[1], "It's a wm class property")
        assertEquals(atomLibrary[Atoms.STRING], changePropertyCall.parameters[2], "The property type is string")
        assertNull(changePropertyCall.parameters[3], "There should be no data to append")
        assertEquals(8, changePropertyCall.parameters[4], "The format is 8")
        assertEquals(PropModeAppend, changePropertyCall.parameters[5], "The mode should be append")
    }

    @Test
    fun `reset the event time`() {
        val system = object : SystemFacadeMock() {
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
        }
        val atomLibrary = AtomLibrary(system)
        val eventBuffer = EventBuffer(system)
        val rootWindowPropertyHandler = RootWindowPropertyHandler(LoggerMock(), system, system.rootWindowId, atomLibrary, eventBuffer)

        val eventTime = EventTime(system, eventBuffer, atomLibrary, rootWindowPropertyHandler)

        system.functionCalls.clear()

        eventTime.resetEventTime()

        assertEquals(123.convert(), eventTime.lastEventTime, "The event time should match with the time of the available event")

        val changePropertyCall = system.functionCalls.removeAt(0)
        assertEquals("changeProperty", changePropertyCall.name, "Change property needs to be called to trigger getting a time")
        assertEquals(rootWindowPropertyHandler.ewmhSupportWindow, changePropertyCall.parameters[0], "Change the property for the EWMH support window")
        assertEquals(atomLibrary[Atoms.WM_CLASS], changePropertyCall.parameters[1], "It's a wm class property")
        assertEquals(atomLibrary[Atoms.STRING], changePropertyCall.parameters[2], "The property type is string")
        assertNull(changePropertyCall.parameters[3], "There should be no data to append")
        assertEquals(8, changePropertyCall.parameters[4], "The format is 8")
        assertEquals(PropModeAppend, changePropertyCall.parameters[5], "The mode should be append")

        assertTrue(system.functionCalls.isEmpty(), "The time should be only requested once")
    }

    @Test
    fun `unset the event time`() {
        val system = object : SystemFacadeMock() {
            var time = 1
            override fun getQueuedEvents(mode: Int): Int {
                time++
                return 1
            }

            override fun nextEvent(event: CPointer<XEvent>): Int {
                event.pointed.type = PropertyNotify
                event.pointed.xproperty.time = time.convert()
                return Success
            }
        }
        val atomLibrary = AtomLibrary(system)
        val eventBuffer = EventBuffer(system)
        val rootWindowPropertyHandler = RootWindowPropertyHandler(LoggerMock(), system, system.rootWindowId, atomLibrary, eventBuffer)

        val eventTime = EventTime(system, eventBuffer, atomLibrary, rootWindowPropertyHandler)

        assertEquals(2.convert(), eventTime.lastEventTime, "The event time should match with the time of the available event")

        eventBuffer.getNextEvent(false)
        eventTime.unsetEventTime()

        assertEquals(3.convert(), eventTime.lastEventTime, "The event time should match with the time of the available event")
    }
}
