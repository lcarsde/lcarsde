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
        fun createNormal(monitorX: Int, monitorY: Int, monitorWidth: Int, monitorHeight: Int): WindowMeasurements {
            return WindowMeasurements(
                monitorX + NORMAL_WINDOW_LEFT_OFFSET,
                monitorY + NORMAL_WINDOW_UPPER_OFFSET,
                monitorWidth - NORMAL_WINDOW_LEFT_OFFSET,
                monitorHeight - NORMAL_WINDOW_UPPER_OFFSET - BAR_GAP_SIZE - BAR_HEIGHT,
                monitorHeight - NORMAL_WINDOW_UPPER_OFFSET
            )
        }

        /** Create window measurements for maximized windows */
        fun createMaximized(monitorX: Int, monitorY: Int, monitorWidth: Int, monitorHeight: Int): WindowMeasurements {
            val upperWindowOffset = BAR_HEIGHT + BAR_GAP_SIZE

            return WindowMeasurements(
                monitorX,
                monitorY + upperWindowOffset,
                monitorWidth,
                monitorHeight - 2 * upperWindowOffset,
                monitorHeight - upperWindowOffset
            )
        }

        /** Create window measurements for fullscreen windows */
        fun createFullscreen(monitorX: Int, monitorY: Int, monitorWidth: Int, monitorHeight: Int): WindowMeasurements {
            return WindowMeasurements(
                monitorX,
                monitorY,
                monitorWidth,
                monitorHeight,
                monitorHeight
            )
        }
    }
}