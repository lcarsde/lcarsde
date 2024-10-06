package de.atennert.lcarswm.monitor

import de.atennert.lcarswm.ScreenMode
import de.atennert.rx.Observable

/**
 * Interface for manager for handling monitors and the current screen mode for those monitors.
 * The screen modes are defined in {@link ScreenMode}
 */
interface MonitorManager<Output> {
    /**
     * Provides the current screen mode.
     */
    val screenModeObs: Observable<ScreenMode>

    /**
     * Provides the list of known active monitors
     */
    val monitorsObs: Observable<List<Monitor<Output>>>
    /**
     * Provides the primary monitor, if there's no monitor marked as primary monitor,
     * the monitor manager will select the first monitor it finds as primary.
     * Provides null if there is no monitor.
     */
    val primaryMonitorObs: Observable<Monitor<Output>?>
    /**
     * Provides the combined size of all monitors, depending on the configured
     * monitor screen size and the positioning of the monitors.
     * Provides (0, 0) if there is no monitor.
     */
    val combinedScreenSizeObs: Observable<Pair<Int, Int>>

    /**
     * Update the list of known active monitors
     */
    fun updateMonitorList()

    /**
     * Toggle between all ScreenModes
     */
    fun toggleScreenMode()

    /**
     * Toggle between ScreenModes that have a frame.
     */
    fun toggleFramedScreenMode()
}