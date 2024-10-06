package de.atennert.lcarswm.monitor

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.api.RandrApi
import de.atennert.rx.BehaviorSubject
import de.atennert.rx.operators.combineLatestWith
import de.atennert.rx.operators.map
import kotlinx.cinterop.*
import xlib.*
import kotlin.math.max

@ExperimentalForeignApi
class MonitorManagerImpl(private val randrApi: RandrApi, private val rootWindowId: Window) : MonitorManager<RROutput> {

    private val lastMonitorBuildersSj = BehaviorSubject<List<Monitor.Builder<RROutput>>>(emptyList())
    private val lastMonitorBuildersObs = lastMonitorBuildersSj.asObservable()

    private val screenModeSj = BehaviorSubject(ScreenMode.NORMAL)
    override val screenModeObs = screenModeSj.asObservable()
    private var screenMode by screenModeSj

    override val monitorsObs = lastMonitorBuildersObs
        .apply(combineLatestWith(screenModeObs))
        .apply(map { (monitorBuilders, screenMode) ->
            monitorBuilders.map { it.setScreenMode(screenMode).build() }
        })

    override val primaryMonitorObs = monitorsObs
        .apply(map { monitors -> monitors.firstOrNull { it.isPrimary } })

    override val combinedScreenSizeObs = monitorsObs
        .apply(map { monitors ->
            monitors.fold(Pair(0, 0)) { (oldWidth, oldHeight), monitor ->
                Pair(
                    max(monitor.x + monitor.width, oldWidth),
                    max(monitor.y + monitor.height, oldHeight)
                )
            }
        })

    override fun updateMonitorList() {
        val monitorData = getMonitorData()
        val monitorIds = getMonitorIds(monitorData)
        val primary = getPrimary(monitorIds)

        val activeMonitorInfos = monitorIds
            .map { monitorId -> Pair(monitorId, randrApi.rGetOutputInfo(monitorData, monitorId)) }
            .filter { (_, outputInfo) -> outputInfo != null && outputInfo.pointed.crtc.convert<Int>() != 0 }

        val monitorNames = activeMonitorInfos
            .map { (_, outputInfo) -> getOutputName(outputInfo!!) }

        lastMonitorBuildersSj.next(activeMonitorInfos
            .map { (monitorId, _) -> monitorId }
            .zip(monitorNames)
            .map { (id, name) -> Monitor.Builder(id, name, id == primary) }
            .zip(activeMonitorInfos.map { it.second })
            .map { (monitor, outputInfo) -> addMeasurementToMonitor(monitor, outputInfo!!.pointed.crtc, monitorData) }
            .sortedBy { (it.y + it.height).toULong().shl(32) + it.x.toULong() })
    }

    private fun getMonitorData(): CPointer<XRRScreenResources> {
        return randrApi.rGetScreenResources(rootWindowId)!!
    }

    private fun getMonitorIds(monitorData: CPointer<XRRScreenResources>): Array<RROutput> {
        val screenResources = monitorData.pointed
        return Array(screenResources.noutput) { screenResources.outputs!![it] }
    }

    private fun getPrimary(monitorIds: Array<RROutput>): RROutput {
        val primaryMonitorId = randrApi.rGetOutputPrimary(rootWindowId)

        return if (monitorIds.contains(primaryMonitorId)) {
            primaryMonitorId
        } else {
            monitorIds[0]
        }
    }

    /**
     * Get the name of the given output.
     */
    private fun getOutputName(outputObject: CPointer<XRROutputInfo>): String {
        val name = outputObject.pointed.name
        val nameArray = ByteArray(outputObject.pointed.nameLen) { name!![it] }

        return nameArray.decodeToString()
    }

    private fun addMeasurementToMonitor(
        monitor: Monitor.Builder<RROutput>,
        crtcReference: RRCrtc,
        monitorData: CPointer<XRRScreenResources>
    ): Monitor.Builder<RROutput> {
        val crtcInfo = randrApi.rGetCrtcInfo(monitorData, crtcReference)!!.pointed

        monitor.setMeasurements(crtcInfo.x, crtcInfo.y, crtcInfo.width.convert(), crtcInfo.height.convert())

        return monitor
    }

    override fun toggleScreenMode() {
        screenMode = when (screenMode) {
            ScreenMode.NORMAL -> ScreenMode.MAXIMIZED
            ScreenMode.MAXIMIZED -> ScreenMode.FULLSCREEN
            ScreenMode.FULLSCREEN -> ScreenMode.NORMAL
        }
    }

    override fun toggleFramedScreenMode() {
        screenMode = if (screenMode == ScreenMode.NORMAL) {
            ScreenMode.MAXIMIZED
        } else {
            ScreenMode.NORMAL
        }
    }
}