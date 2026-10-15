package de.atennert.lcarswm.drawing

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarswm.*
import de.atennert.lcarswm.drivenPorts.drawing.DrawFactory
import de.atennert.lcarswm.drivenPorts.drawing.Surface
import de.atennert.lcarswm.drivenPorts.drawing.TextAlignment
import de.atennert.lcarswm.settings.GeneralSetting
import de.atennert.lcarswm.system.api.WindowUtilApi
import de.atennert.lcarswm.window.WindowMeasurements
import gtk.Screen
import gtk.Window
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
class FrameDrawer(
    private val windowUtilApi: WindowUtilApi,
    private val screen: Screen,
    settings: Map<GeneralSetting, String>
) : IFrameDrawer {
    private val drawFactory by inject<DrawFactory>()

    private val fontFamily = settings[GeneralSetting.FONT]

    override fun drawFrame(
        measurements: WindowMeasurements,
        screenMode: ScreenMode,
        isFocused: Boolean,
        title: String,
        titleBar: Window,
    ) {
        val surface = drawFactory.createSurface(
            Surface.Configuration(
                windowUtilApi.display,
                drawable = titleBar,
                visual = screen.root_visual,
                width = measurements.width,
                height = BAR_HEIGHT + TITLE_BAR_OFFSET
            )
        )
        surface.setSize(measurements.width, BAR_HEIGHT + TITLE_BAR_OFFSET)
        val cr = surface.createSession()

        if (screenMode == ScreenMode.NORMAL) {
            val primBarWidth = LOWER_CORNER_WIDTH - NORMAL_WINDOW_LEFT_OFFSET
            val secBarWidth = measurements.width - primBarWidth - 2 * BAR_GAP_SIZE

            cr.drawRectangle(0, TITLE_BAR_OFFSET, primBarWidth, BAR_HEIGHT, COLOR_NORMAL_CORNER_3)
            cr.drawRectangle(
                primBarWidth + BAR_GAP_SIZE,
                TITLE_BAR_OFFSET,
                secBarWidth,
                BAR_HEIGHT,
                COLOR_NORMAL_BAR_DOWN
            )
        } else {
            val barWidth = measurements.width - (BAR_END_WIDTH + BAR_GAP_SIZE) * 2
            cr.drawRectangle(BAR_END_WIDTH + BAR_GAP_SIZE, TITLE_BAR_OFFSET, barWidth, BAR_HEIGHT, COLOR_MAX_BAR_DOWN)

            cr.drawBarEndLeft(0, TITLE_BAR_OFFSET, BAR_END_WIDTH, BAR_HEIGHT, COLOR_BAR_ENDS)
            cr.drawBarEndRight(
                measurements.width - BAR_END_WIDTH,
                TITLE_BAR_OFFSET,
                BAR_END_WIDTH,
                BAR_HEIGHT,
                COLOR_BAR_ENDS
            )
        }

        val textColor = if (isFocused) {
            COLOR_ACTIVE_TITLE
        } else {
            COLOR_INACTIVE_TITLE
        }

        cr.drawText(
            title,
            fontFamily,
            measurements.width - BAR_END_WIDTH,
            TITLE_BAR_OFFSET,
            BAR_HEIGHT,
            TextAlignment.RIGHT,
            textColor,
            COLOR_BACKGROUND
        )

        cr.dispose()
        surface.dispose()
    }
}