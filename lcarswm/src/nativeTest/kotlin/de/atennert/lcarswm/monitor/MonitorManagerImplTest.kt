package de.atennert.lcarswm.monitor

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.SystemFacadeMock
import de.atennert.rx.NextObserver
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import xlib.RROutput
import xlib.Window
import xlib.XRROutputInfo
import xlib.XRRScreenResources
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExperimentalForeignApi
class MonitorManagerImplTest {
    @Test
    fun `update monitor list`() {
        val systemApi = SystemFacadeMock()
        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val monitors = mutableListOf<List<Monitor<RROutput>>>()
        val monitorSub = monitorManager.monitorsObs.subscribe(NextObserver(monitors::add))
        val primaries = mutableListOf<Monitor<RROutput>?>()
        val primarySub = monitorManager.primaryMonitorObs.subscribe(NextObserver(primaries::add))

        monitorManager.updateMonitorList()

        assertEquals( systemApi.outputs.toList(), monitors.last().map { it.id }, "The monitor IDs should match")
        assertEquals( systemApi.outputNames.toList(), monitors.last().map { it.name }, "The monitor names should match")
        assertEquals(systemApi.primaryOutput, primaries.last()?.id, "The ID of the primary monitor should match")

        monitors.last().shouldContain(primaries.last())

        monitors.size.shouldBe(2) // initial list + update
        primaries.size.shouldBe(2) // initial monitor + update

        monitorSub.unsubscribe()
        primarySub.unsubscribe()
    }

    @Test
    fun `check the first monitor becomes primary if there's no primary`() {
        val systemApi = object : SystemFacadeMock() {
            override fun rGetOutputPrimary(window: Window): RROutput = 0.convert()
        }
        val monitorManager: MonitorManager<RROutput> = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val monitors = mutableListOf<List<Monitor<RROutput>>>()
        val monitorSub = monitorManager.monitorsObs.subscribe(NextObserver(monitors::add))
        val primaries = mutableListOf<Monitor<RROutput>?>()
        val primarySub = monitorManager.primaryMonitorObs.subscribe(NextObserver(primaries::add))

        monitorManager.updateMonitorList()

        assertEquals(monitors.last()[0].id, primaries.last()?.id, "The first monitor shall become the primary, if there's no primary")

        monitorSub.unsubscribe()
        primarySub.unsubscribe()
    }

    @Test
    fun `add monitor measurements`() {
        val systemApi = SystemFacadeMock()

        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val monitors = mutableListOf<List<Monitor<RROutput>>>()
        val monitorSub = monitorManager.monitorsObs.subscribe(NextObserver(monitors::add))

        monitorManager.updateMonitorList()

        monitors.last().forEachIndexed { index, monitor ->
            val totalMeasurements = systemApi.crtcInfos[index]
            assertEquals(totalMeasurements[0], monitor.x, "The expected x value needs to match the monitors x value")
            assertEquals(totalMeasurements[1], monitor.y, "The expected y value needs to match the monitors y value")
            assertEquals(totalMeasurements[2], monitor.width, "The expected width needs to match the monitors width")
            assertEquals(totalMeasurements[3], monitor.height, "The expected height needs to match the monitors height")
        }

        monitorSub.unsubscribe()
    }

    @Test
    fun `don't add monitors without crtc`() {
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

        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val monitors = mutableListOf<List<Monitor<RROutput>>>()
        val monitorSub = monitorManager.monitorsObs.subscribe(NextObserver(monitors::add))

        monitorManager.updateMonitorList()

        assertContentEquals(listOf(systemApi.outputs[0]), monitors.last().map { it.id }, "The monitor with crtc should be in the monitor list ${monitors.last()}")

        monitorSub.unsubscribe()
    }

    @Test
    fun `get the combined monitor screen size`() {
        val systemApi = SystemFacadeMock()

        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val measurements = mutableListOf<Pair<Int, Int>>()
        val measurementSub = monitorManager.combinedScreenSizeObs.subscribe(NextObserver(measurements::add))

        monitorManager.updateMonitorList()

        val (width, height) = measurements.last()

        assertEquals(2000, width, "The width should match the combined screen width")

        assertEquals(500, height, "The height should match the combined screen height")

        measurementSub.unsubscribe()
    }

    @Test
    fun `check that default screen mode is normal`() {
        val systemApi = SystemFacadeMock()

        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val screenModes = mutableListOf<ScreenMode>()
        val screenModeSub = monitorManager.screenModeObs.subscribe(NextObserver(screenModes::add))

        assertContentEquals(listOf(ScreenMode.NORMAL), screenModes, "The default screen mode should be normal")

        screenModeSub.unsubscribe()
    }

    @Test
    fun `change the screen mode`() {
        val systemApi = SystemFacadeMock()

        val monitorManager = MonitorManagerImpl(systemApi, systemApi.rootWindowId)

        val screenModes = mutableListOf<ScreenMode>()
        val screenModeSub = monitorManager.screenModeObs.subscribe(NextObserver(screenModes::add))

        listOf(ScreenMode.MAXIMIZED, ScreenMode.FULLSCREEN, ScreenMode.NORMAL)
            .forEach { screenMode ->
                monitorManager.toggleScreenMode()
                assertEquals(screenMode, screenModes.last(), "It should switch to $screenMode")
            }

        screenModeSub.unsubscribe()
    }
}
