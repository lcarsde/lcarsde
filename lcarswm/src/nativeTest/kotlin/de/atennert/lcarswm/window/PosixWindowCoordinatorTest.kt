package de.atennert.lcarswm.window

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.drawing.UIDrawingMock
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.monitor.MonitorManagerMock
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.lcarswm.system.SystemCallMocker
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.*
import xlib.Display
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExperimentalForeignApi
class PosixWindowCoordinatorTest : SystemCallMocker() {
    private val eventStore = EventStore()
    private var display: CPointer<Display>? = null

    @BeforeTest
    override fun setup() {
        super.setup()
        display = nativeHeap.allocPointerTo<Display>().value
    }

    @AfterTest
    override fun teardown() {
        closeClosables()
        display?.let(nativeHeap::free)
        super.teardown()
    }

    @Test
    fun `add windows to a monitor`() {
        val monitorManager = MonitorManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()
        val primaryMonitor = monitorManager.lastMonitorBuilders[0]

        PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )

        eventStore.mapSj.next(42.convert())

        val window = windowList.get(42.convert()) as FakeManagedWindow
        val windowMeasurements = WindowMeasurements.createNormal(
            primaryMonitor.x,
            primaryMonitor.y,
            primaryMonitor.width,
            primaryMonitor.height,
        )

        window.functionCalls.last().shouldBe(FunctionCall("open", windowMeasurements, ScreenMode.NORMAL))
    }

    @Test
    fun `remove window from coordinator`() {
        val monitorManager = MonitorManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )

        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        eventStore.unmapSj.next(42.convert())

        windowList.get(42.convert()).shouldBeNull()
        window.functionCalls.last().shouldBe(FunctionCall("close"))
    }

    @Test
    fun `handle monitor updates`() {
        val monitorManager = MonitorManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        val newMonitor = MonitorManagerMock.createMonitorBuilder(1, width = 1280, height = 1024, isPrimary = true)
        monitorManager.lastMonitorBuildersSj.next(listOf(newMonitor))

        val measurments = WindowMeasurements.createNormal(
            newMonitor.x,
            newMonitor.y,
            newMonitor.width,
            newMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.NORMAL))
    }

    @Test
    fun `move window to next monitor`() {
        val monitorManager = MonitorManagerMock()
        val primaryMonitor = monitorManager.lastMonitorBuilders[0]
        val secondaryMonitor = MonitorManagerMock.createMonitorBuilder(2, x = 800)
        val tertiaryMonitor = MonitorManagerMock.createMonitorBuilder(2, y = 600)
        monitorManager.lastMonitorBuildersSj.next(listOf(primaryMonitor, secondaryMonitor, tertiaryMonitor))

        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        val windowCoordinator = PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        windowCoordinator.moveWindowToNextMonitor(window.id)
        var measurments = WindowMeasurements.createMaximized(
            secondaryMonitor.x,
            secondaryMonitor.y,
            secondaryMonitor.width,
            secondaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.MAXIMIZED))

        windowCoordinator.moveWindowToNextMonitor(window.id)
        measurments = WindowMeasurements.createMaximized(
            tertiaryMonitor.x,
            tertiaryMonitor.y,
            tertiaryMonitor.width,
            tertiaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.MAXIMIZED))

        windowCoordinator.moveWindowToNextMonitor(window.id)
        measurments = WindowMeasurements.createNormal(
            primaryMonitor.x,
            primaryMonitor.y,
            primaryMonitor.width,
            primaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.NORMAL))
    }

    @Test
    fun `move window to previous monitor`() {
        val monitorManager = MonitorManagerMock()
        val primaryMonitor = monitorManager.lastMonitorBuilders[0]
        val secondaryMonitor = MonitorManagerMock.createMonitorBuilder(2, x = 800)
        val tertiaryMonitor = MonitorManagerMock.createMonitorBuilder(2, y = 600)
        monitorManager.lastMonitorBuildersSj.next(listOf(primaryMonitor, secondaryMonitor, tertiaryMonitor))

        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        val windowCoordinator = PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        windowCoordinator.moveWindowToPreviousMonitor(window.id)
        var measurments = WindowMeasurements.createMaximized(
            tertiaryMonitor.x,
            tertiaryMonitor.y,
            tertiaryMonitor.width,
            tertiaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.MAXIMIZED))

        windowCoordinator.moveWindowToPreviousMonitor(window.id)
        measurments = WindowMeasurements.createMaximized(
            secondaryMonitor.x,
            secondaryMonitor.y,
            secondaryMonitor.width,
            secondaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.MAXIMIZED))

        windowCoordinator.moveWindowToPreviousMonitor(window.id)
        measurments = WindowMeasurements.createNormal(
            primaryMonitor.x,
            primaryMonitor.y,
            primaryMonitor.width,
            primaryMonitor.height
        )
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.NORMAL))
    }

    @Test
    fun `test realign windows`() {
        val monitorManager = MonitorManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            LoggerMock(),
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        monitorManager.screenMode = ScreenMode.MAXIMIZED
        var measurments = WindowMeasurements(0, 48, 800, 504, 552)
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.MAXIMIZED))

        monitorManager.screenMode = ScreenMode.FULLSCREEN
        measurments = WindowMeasurements(0, 0, 800, 600, 600)
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.FULLSCREEN))

        monitorManager.screenMode = ScreenMode.NORMAL
        measurments = WindowMeasurements(232, 240, 568, 312, 360)
        window.functionCalls.last().shouldBe(FunctionCall("moveResize", measurments, ScreenMode.NORMAL))
    }
}
