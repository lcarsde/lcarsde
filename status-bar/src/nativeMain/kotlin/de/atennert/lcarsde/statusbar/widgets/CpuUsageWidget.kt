package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import statusbar.GtkCssProvider

@ExperimentalForeignApi
class CpuUsageWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>) :
    RadarGraphWidget(widgetConfiguration, cssProvider, 500, 100) {

    data class CpuUse(val idle: Float, val total: Float) {
        operator fun minus(cpuUse2: CpuUse) = CpuUse(cpuUse2.idle - this.idle, cpuUse2.total - this.total)
    }

    override val attentionValue = 60.0
    override val warningValue = 80.0

    private var lastCpuUses: List<CpuUse> = emptyList()

    override fun getData(): List<Double> = getCpuUtilization()

    private fun getCpuUtilization(): List<Double> {
        val statData = readFile("/proc/stat")?.lines() ?: return emptyList()
        val cpuData = statData
            .filter { it.startsWith("cpu") }
            .drop(1)
            .map {
                it.trim()
                    .split(' ')
                    .drop(1)
                    .map(String::toFloat)
            }
        val cpuUses = cpuData.map { CpuUse(it[3] + it[4], it.sum()) }

        val cpuUseDelta = if (lastCpuUses.isEmpty()) {
            List(cpuUses.size) { CpuUse(1f, 1f) }
        } else {
            cpuUses.zip(lastCpuUses) { currentCpuUse, lastCpuUse -> currentCpuUse - lastCpuUse }
        }
        lastCpuUses = cpuUses

        return cpuUseDelta.map { (idle, total) ->
            try {
                100 * (1.0 - idle / total)
            } catch (e: ArithmeticException) {
                100 * (1.0 - idle / 0.001)
            }
        }
    }
}