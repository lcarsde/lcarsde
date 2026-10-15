package de.atennert.lcarswm.screen

import de.atennert.lcarswm.ScreenMode
import de.atennert.rx.Observable

/**
 * Interface for manager for handling screens and the current screen mode for those screens.
 * The screen modes are defined in {@link ScreenMode}
 */
interface ScreenManager<Output> {
    /**
     * Provides the current screen mode.
     */
    val screenModeObs: Observable<ScreenMode>

    /**
     * Provides the list of known active screens
     */
    val screensObs: Observable<List<Screen<Output>>>

    /**
     * Provides the primary screen, if there's no screen marked as primary screen,
     * the screen manager will select the first screen it finds as primary.
     * Provides null if there is no screen.
     */
    val primaryScreenObs: Observable<Screen<Output>?>

    /**
     * Provides the combined size of all screens, depending on the configured
     * screen size and the positioning of the screens.
     * Provides (0, 0) if there is no screen.
     */
    val combinedScreenSizeObs: Observable<Pair<Int, Int>>

    /**
     * Update the list of known active screens
     */
    fun updateScreenList()

    /**
     * Toggle between all ScreenModes
     */
    fun toggleScreenMode()

    /**
     * Toggle between ScreenModes that have a frame.
     */
    fun toggleFramedScreenMode()
}