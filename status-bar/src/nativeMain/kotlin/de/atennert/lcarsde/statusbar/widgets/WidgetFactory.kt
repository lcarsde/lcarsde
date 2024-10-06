package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import statusbar.GtkCssProvider

/**
 * Used to create a widget based on a configuration entry.
 */
@ExperimentalForeignApi
class WidgetFactory(private val cssProvider: CPointer<GtkCssProvider>) {

    fun createWidget(configuration: WidgetConfiguration): StatusWidget? = when (configuration.name) {
        "LcarsdeStatusTime"         -> TimeWidget(configuration, cssProvider)
        "LcarsdeStatusDate"         -> DateWidget(configuration, cssProvider)
        "LcarsdeStatusStardate"     -> StardateWidget(configuration, cssProvider)
        "LcarsdeStatusButton"       -> ButtonWidget(configuration, cssProvider)
        "LcarsdeStatusTemperature"  -> TemperatureGraphWidget(configuration, cssProvider)
        "LcarsdeStatusCpuUsage"     -> CpuUsageWidget(configuration, cssProvider)
        "LcarsdeBatteryStatus"      -> BatteryWidget(configuration, cssProvider)
        "LcarsdeWifiStatus"         -> WifiWidget(configuration, cssProvider)
        "LcarsdeEthStatus"          -> EthernetWidget(configuration, cssProvider)
        "LcarsdeStatusAudio"        -> AudioWidget(configuration, cssProvider)
        "LcarsdeStatusMemory"       -> MemoryWidget(configuration, cssProvider)
        else -> null
    }
}