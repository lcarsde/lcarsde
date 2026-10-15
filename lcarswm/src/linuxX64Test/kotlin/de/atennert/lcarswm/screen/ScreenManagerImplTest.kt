package de.atennert.lcarswm.screen

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.SystemFacadeMock
import de.atennert.rx.NextObserver
import gtk.Window
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import xlib.RROutput
import xlib.XRROutputInfo
import xlib.XRRScreenResources
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExperimentalForeignApi
class ScreenManagerImplTest {
    @Test
    fun `update screen list`() {
        val systemApi = SystemFacadeMock()
        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screens = mutableListOf<List<Screen<RROutput>>>()
        val screenSub = screenManager.screensObs.subscribe(NextObserver(screens::add))
        val primaries = mutableListOf<Screen<RROutput>?>()
        val primarySub = screenManager.primaryScreenObs.subscribe(NextObserver(primaries::add))

        screenManager.updateScreenList()

        assertEquals(
            systemApi.outputs.toList(),
            screens.last().map { it.id },
            "The screen IDs should match"
        )
        assertEquals(
            systemApi.outputNames.toList(),
            screens.last().map { it.name },
            "The screen names should match"
        )
        assertEquals(
            systemApi.primaryOutput,
            primaries.last()?.id,
            "The ID of the primary screen should match"
        )

        screens.last().shouldContain(primaries.last())

        screens.size.shouldBe(2) // initial list + update
        primaries.size.shouldBe(2) // initial screen + update

        screenSub.unsubscribe()
        primarySub.unsubscribe()
    }

    @Test
    fun `check the first screen becomes primary if there's no primary`() {
        val systemApi = object : SystemFacadeMock() {
            override fun rGetOutputPrimary(window: Window): RROutput = 0.convert()
        }
        val screenManager: ScreenManager<RROutput> = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screens = mutableListOf<List<Screen<RROutput>>>()
        val screenSub = screenManager.screensObs.subscribe(NextObserver(screens::add))
        val primaries = mutableListOf<Screen<RROutput>?>()
        val primarySub = screenManager.primaryScreenObs.subscribe(NextObserver(primaries::add))

        screenManager.updateScreenList()

        assertEquals(
            screens.last()[0].id,
            primaries.last()?.id,
            "The first screen shall become the primary, if there's no primary"
        )

        screenSub.unsubscribe()
        primarySub.unsubscribe()
    }

    @Test
    fun `add screen measurements`() {
        val systemApi = SystemFacadeMock()

        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screens = mutableListOf<List<Screen<RROutput>>>()
        val screenSub = screenManager.screensObs.subscribe(NextObserver(screens::add))

        screenManager.updateScreenList()

        screens.last().forEachIndexed { index, screen ->
            val totalMeasurements = systemApi.crtcInfos[index]
            assertEquals(totalMeasurements[0], screen.x, "The expected x value needs to match the screens x value")
            assertEquals(totalMeasurements[1], screen.y, "The expected y value needs to match the screens y value")
            assertEquals(totalMeasurements[2], screen.width, "The expected width needs to match the screens width")
            assertEquals(totalMeasurements[3], screen.height, "The expected height needs to match the screens height")
        }

        screenSub.unsubscribe()
    }

    @Test
    fun `don't add screens without crtc`() {
        val systemApi = object : SystemFacadeMock() {
            override fun rGetOutputInfo(
                resources: CPointer<XRRScreenResources>,
                output: RROutput
            ): CPointer<XRROutputInfo>? {
                val result = super.rGetOutputInfo(resources, output)
                if (output == outputs[1]) {
                    result!!.pointed.crtc = 0.convert()
                }
                return result
            }
        }

        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screens = mutableListOf<List<Screen<RROutput>>>()
        val screenSub = screenManager.screensObs.subscribe(NextObserver(screens::add))

        screenManager.updateScreenList()

        assertContentEquals(
            listOf(systemApi.outputs[0]),
            screens.last().map { it.id },
            "The screen with crtc should be in the screen list ${screens.last()}"
        )

        screenSub.unsubscribe()
    }

    @Test
    fun `get the combined screen screen size`() {
        val systemApi = SystemFacadeMock()

        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val measurements = mutableListOf<Pair<Int, Int>>()
        val measurementSub = screenManager.combinedScreenSizeObs.subscribe(NextObserver(measurements::add))

        screenManager.updateScreenList()

        val (width, height) = measurements.last()

        assertEquals(2000, width, "The width should match the combined screen width")

        assertEquals(500, height, "The height should match the combined screen height")

        measurementSub.unsubscribe()
    }

    @Test
    fun `check that default screen mode is normal`() {
        val systemApi = SystemFacadeMock()

        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screenModes = mutableListOf<ScreenMode>()
        val screenModeSub = screenManager.screenModeObs.subscribe(NextObserver(screenModes::add))

        assertContentEquals(listOf(ScreenMode.NORMAL), screenModes, "The default screen mode should be normal")

        screenModeSub.unsubscribe()
    }

    @Test
    fun `change the screen mode`() {
        val systemApi = SystemFacadeMock()

        val screenManager = ScreenManagerImpl(systemApi, systemApi.rootWindowId)

        val screenModes = mutableListOf<ScreenMode>()
        val screenModeSub = screenManager.screenModeObs.subscribe(NextObserver(screenModes::add))

        listOf(ScreenMode.MAXIMIZED, ScreenMode.FULLSCREEN, ScreenMode.NORMAL)
            .forEach { screenMode ->
                screenManager.toggleScreenMode()
                assertEquals(screenMode, screenModes.last(), "It should switch to $screenMode")
            }

        screenModeSub.unsubscribe()
    }
}
