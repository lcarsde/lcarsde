package de.atennert.lcarswm.monitor

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.rx.BehaviorSubject
import de.atennert.rx.operators.combineLatestWith
import de.atennert.rx.operators.map
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.RROutput
import kotlin.math.max

@ExperimentalForeignApi
open class MonitorManagerMock : MonitorManager<RROutput> {
    val functionCalls = mutableListOf<FunctionCall>()

    val lastMonitorBuildersSj = BehaviorSubject(listOf(createMonitorBuilder(1, isPrimary = true)))
    private val lastMonitorBuildersObs = lastMonitorBuildersSj.asObservable()
    val lastMonitorBuilders by  lastMonitorBuildersSj

    private val screenModeSj = BehaviorSubject(ScreenMode.NORMAL)
    override val screenModeObs = screenModeSj.asObservable()
    var screenMode by screenModeSj

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
        functionCalls.add(FunctionCall("updateMonitorList"))
    }

    override fun toggleScreenMode() {
        functionCalls.add(FunctionCall("toggleScreenMode"))
    }

    override fun toggleFramedScreenMode() {
        functionCalls.add(FunctionCall("toggleFramedScreenMode"))
    }

    companion object {
        fun createMonitorBuilder(
            id: Int,
            name: String = "",
            x: Int = 0,
            y: Int = 0,
            width: Int = 800,
            height: Int = 600,
            isPrimary: Boolean = false
        ): Monitor.Builder<RROutput> {
            return Monitor.Builder<RROutput>(id.convert(), name, isPrimary)
                .setMeasurements(x, y, width, height)
        }
    }
}