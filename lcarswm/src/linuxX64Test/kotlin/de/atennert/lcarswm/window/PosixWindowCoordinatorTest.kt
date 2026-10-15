package de.atennert.lcarswm.window

import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.lifecycle.closeClosables
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.drawing.UIDrawingMock
import de.atennert.lcarswm.events.EventStore
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.screen.ScreenManagerMock
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.lcarswm.system.SystemCallMocker
import gtk.Display
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.*
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
        ServiceLocator.provide<Logger> { LoggerMock() }
    }

    @AfterTest
    override fun teardown() {
        closeClosables()
        display?.let(nativeHeap::free)
        ServiceLocator.clear()
        super.teardown()
    }

    @Test
    fun `add windows to a screen`() {
        val screenManager = ScreenManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()
        val primaryScreen = screenManager.lastScreenBuilders[0]

        PosixWindowCoordinator(
            eventStore,
            screenManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )

        eventStore.mapSj.next(42.convert())

        val window = windowList.get(42.convert()) as FakeManagedWindow
        val windowMeasurements = WindowMeasurements.createNormal(
            primaryScreen.x,
            primaryScreen.y,
            primaryScreen.width,
            primaryScreen.height,
        )

        window.functionCalls.last().shouldBe(
            FunctionCall(
                "open",
                windowMeasurements,
                ScreenMode.NORMAL
            )
        )
    }

    @Test
    fun `remove window from coordinator`() {
        val screenManager = ScreenManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            eventStore,
            screenManager,
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
    fun `handle screen updates`() {
        val screenManager = ScreenManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            eventStore,
            screenManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        val newScreen =
            ScreenManagerMock.createScreenBuilder(
                1,
                width = 1280,
                height = 1024,
                isPrimary = true
            )
        screenManager.lastScreenBuildersSj.next(listOf(newScreen))

        val measurments = WindowMeasurements.createNormal(
            newScreen.x,
            newScreen.y,
            newScreen.width,
            newScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.NORMAL
            )
        )
    }

    @Test
    fun `move window to next screen`() {
        val screenManager = ScreenManagerMock()
        val primaryScreen = screenManager.lastScreenBuilders[0]
        val secondaryScreen =
            ScreenManagerMock.createScreenBuilder(2, x = 800)
        val tertiaryScreen =
            ScreenManagerMock.createScreenBuilder(2, y = 600)
        screenManager.lastScreenBuildersSj.next(listOf(primaryScreen, secondaryScreen, tertiaryScreen))

        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        val windowCoordinator = PosixWindowCoordinator(
            eventStore,
            screenManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        windowCoordinator.moveWindowToNextScreen(window.id)
        var measurments = WindowMeasurements.createMaximized(
            secondaryScreen.x,
            secondaryScreen.y,
            secondaryScreen.width,
            secondaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.MAXIMIZED
            )
        )

        windowCoordinator.moveWindowToNextScreen(window.id)
        measurments = WindowMeasurements.createMaximized(
            tertiaryScreen.x,
            tertiaryScreen.y,
            tertiaryScreen.width,
            tertiaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.MAXIMIZED
            )
        )

        windowCoordinator.moveWindowToNextScreen(window.id)
        measurments = WindowMeasurements.createNormal(
            primaryScreen.x,
            primaryScreen.y,
            primaryScreen.width,
            primaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.NORMAL
            )
        )
    }

    @Test
    fun `move window to previous screen`() {
        val screenManager = ScreenManagerMock()
        val primaryScreen = screenManager.lastScreenBuilders[0]
        val secondaryScreen =
            ScreenManagerMock.createScreenBuilder(2, x = 800)
        val tertiaryScreen =
            ScreenManagerMock.createScreenBuilder(2, y = 600)
        screenManager.lastScreenBuildersSj.next(listOf(primaryScreen, secondaryScreen, tertiaryScreen))

        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        val windowCoordinator = PosixWindowCoordinator(
            eventStore,
            screenManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        windowCoordinator.moveWindowToPreviousScreen(window.id)
        var measurments = WindowMeasurements.createMaximized(
            tertiaryScreen.x,
            tertiaryScreen.y,
            tertiaryScreen.width,
            tertiaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.MAXIMIZED
            )
        )

        windowCoordinator.moveWindowToPreviousScreen(window.id)
        measurments = WindowMeasurements.createMaximized(
            secondaryScreen.x,
            secondaryScreen.y,
            secondaryScreen.width,
            secondaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.MAXIMIZED
            )
        )

        windowCoordinator.moveWindowToPreviousScreen(window.id)
        measurments = WindowMeasurements.createNormal(
            primaryScreen.x,
            primaryScreen.y,
            primaryScreen.width,
            primaryScreen.height
        )
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.NORMAL
            )
        )
    }

    @Test
    fun `test realign windows`() {
        val screenManager = ScreenManagerMock()
        val windowFactory = FakeWindowFactory()
        val windowList = WindowList()
        val rootWindowDrawer = UIDrawingMock()

        PosixWindowCoordinator(
            eventStore,
            screenManager,
            windowFactory,
            windowList,
            rootWindowDrawer,
            display,
        )
        eventStore.mapSj.next(42.convert())
        val window = windowList.get(42.convert()) as FakeManagedWindow

        screenManager.screenMode = ScreenMode.MAXIMIZED
        var measurments = WindowMeasurements(0, 48, 800, 504, 552)
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.MAXIMIZED
            )
        )

        screenManager.screenMode = ScreenMode.FULLSCREEN
        measurments = WindowMeasurements(0, 0, 800, 600, 600)
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.FULLSCREEN
            )
        )

        screenManager.screenMode = ScreenMode.NORMAL
        measurments = WindowMeasurements(232, 240, 568, 312, 360)
        window.functionCalls.last().shouldBe(
            FunctionCall(
                "moveResize",
                measurments,
                ScreenMode.NORMAL
            )
        )
    }
}
