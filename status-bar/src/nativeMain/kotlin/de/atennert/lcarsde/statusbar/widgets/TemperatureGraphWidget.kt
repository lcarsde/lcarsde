package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.opendir
import platform.posix.readdir
import statusbar.GtkCssProvider
import kotlin.collections.set

@ExperimentalForeignApi
class TemperatureGraphWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : RadarGraphWidget(widgetConfiguration, cssProvider, 5000, 125) {

    private val tempRegex = Regex("^\\d+$")

    override val attentionValue = 60.0
    override val warningValue = 80.0

    override fun getData(): List<Double> {
        return getTemperatures().entries
            .sortedBy { it.key }
            .map { it.value.toDouble() }
    }

    /**
     * Get every /sys/class/thermal/thermal_zone* directory
     * and read type and temp
     * and set the data.
     */
    private fun getTemperatures(): Map<String, Int> {
        val basePath = "/sys/class/thermal"
        val temperatures = HashMap<String, Int>()
        var unknownIndex = 0

        try {
            opendir(basePath)?.let { dir ->
                while (true) {
                    val subDir = readdir(dir)?.pointed?.d_name?.toKString() ?: break

                    if (!subDir.startsWith("thermal_zone")) {
                        continue
                    }

                    try {
                        val type = readFile("$basePath/$subDir/type")
                            ?.let { it.ifEmpty { "unknown${unknownIndex++}" } }
                            ?: continue

                        val temp = readFile("$basePath/$subDir/temp")
                            ?.let { if (it.matches(tempRegex)) it else null }
                            ?.toInt()
                            ?: continue

                        temperatures[type] = temp / 1000
                    } catch (e: Throwable) {
                        println("Unable to read values from $subDir: ${e.message}")
                    }
                }
                closedir(dir)
            }
        } catch (e: Throwable) {
            println("Unable to read temperatures: ${e.message}")
        }

        return temperatures
    }
}