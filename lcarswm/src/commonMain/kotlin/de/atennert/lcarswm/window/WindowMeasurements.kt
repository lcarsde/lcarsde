package de.atennert.lcarswm.window

import de.atennert.lcarswm.BAR_GAP_SIZE
import de.atennert.lcarswm.BAR_HEIGHT
import de.atennert.lcarswm.NORMAL_WINDOW_LEFT_OFFSET
import de.atennert.lcarswm.NORMAL_WINDOW_UPPER_OFFSET

/**
 * Container class for window measurements.
 */
data class WindowMeasurements(val x: Int, val y: Int, val width: Int, val height: Int, val frameHeight: Int) {

    companion object {
        /** Create window measurements for normal mode windows (primary screen exclusive mode) */
        fun createNormal(screenX: Int, screenY: Int, screenWidth: Int, screenHeight: Int): WindowMeasurements {
            return WindowMeasurements(
                screenX + NORMAL_WINDOW_LEFT_OFFSET,
                screenY + NORMAL_WINDOW_UPPER_OFFSET,
                screenWidth - NORMAL_WINDOW_LEFT_OFFSET,
                screenHeight - NORMAL_WINDOW_UPPER_OFFSET - BAR_GAP_SIZE - BAR_HEIGHT,
                screenHeight - NORMAL_WINDOW_UPPER_OFFSET
            )
        }

        /** Create window measurements for maximized windows */
        fun createMaximized(screenX: Int, screenY: Int, screenWidth: Int, screenHeight: Int): WindowMeasurements {
            val upperWindowOffset = BAR_HEIGHT + BAR_GAP_SIZE

            return WindowMeasurements(
                screenX,
                screenY + upperWindowOffset,
                screenWidth,
                screenHeight - 2 * upperWindowOffset,
                screenHeight - upperWindowOffset
            )
        }

        /** Create window measurements for fullscreen windows */
        fun createFullscreen(screenX: Int, screenY: Int, screenWidth: Int, screenHeight: Int): WindowMeasurements {
            return WindowMeasurements(
                screenX,
                screenY,
                screenWidth,
                screenHeight,
                screenHeight
            )
        }
    }
}