package de.atennert.lcarswm.drawing

import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarswm.*
import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.settings.GeneralSetting
import de.atennert.lcarswm.system.api.WindowUtilApi
import de.atennert.rx.NextObserver
import de.atennert.rx.Subject
import de.atennert.rx.operators.map
import de.atennert.rx.operators.withLatestFrom
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput
import xlib.Screen

/**
 * Class for drawing the root window decorations.
 */
@ExperimentalForeignApi
class RootWindowDrawer(
    private val windowUtilApi: WindowUtilApi,
    monitorManager: MonitorManager<RROutput>,
    private val screen: Screen,
    settings: Map<GeneralSetting, String>
) : UIDrawing {
    private val drawFactory by inject<DrawFactory>()
    private val logoText: String = settings[GeneralSetting.TITLE] ?: "LCARS"

    private val triggerDrawSj = Subject<Unit>()

    private var cairoSurface: Surface? = null
    private val wmLogoPath = settings[GeneralSetting.TITLE_IMAGE]
    private val fontFamily = settings[GeneralSetting.FONT]

    private val logo = wmLogoPath?.let(drawFactory::createImageFromFile)


    init {
        triggerDrawSj
            .withLatestFrom(monitorManager.monitorsObs)
            .map { it.v2 }
            .withLatestFrom(monitorManager.combinedScreenSizeObs)
            .subscribe(NextObserver { (monitors, combinedScreenSize) ->
                internalDrawWindowManagerFrame(monitors, combinedScreenSize)
            })
            .closeWith {
                this.unsubscribe()
                logo?.dispose()
                cairoSurface?.dispose()
            }
    }

    override fun drawWindowManagerFrame() {
        triggerDrawSj.next(Unit)
    }

    private fun internalDrawWindowManagerFrame(monitors: List<Monitor<*>>, combinedScreenSize: Pair<Int, Int>) {
        val (combinedWidth, combinedHeight) = combinedScreenSize
        if (cairoSurface == null) {
            cairoSurface = drawFactory.createSurface(
                Surface.Configuration(
                    windowUtilApi.display,
                    screen.root,
                    screen.root_visual,
                    combinedWidth,
                    combinedHeight,
                )
            )
        }
        cairoSurface?.setSize(combinedWidth, combinedHeight)

        cairoSurface?.createSession()?.let { cr ->
            monitors.forEach {
                when (it.screenMode) { // -> with latest from
                    ScreenMode.NORMAL -> drawNormalFrame(it, cr)
                    ScreenMode.MAXIMIZED -> drawMaximizedFrame(it, cr)
                    ScreenMode.FULLSCREEN -> clearScreen(it, cr)
                }
            }
            cr.dispose()
        }
    }

    private fun drawLogo(cr: Session, x: Int, y: Int) {
        if (logo == null) return

        cr.drawRectangle(x, y, logo.width + 16, BAR_HEIGHT, COLOR_BACKGROUND)
        cr.drawImage(logo, x + 8, y)
    }

    private fun drawMaximizedFrame(monitor: Monitor<*>, cr: Session) {
        clearScreen(monitor, cr)

        cr.drawBarEndLeft(monitor.x, monitor.y, COLOR_BAR_ENDS)
        cr.drawBarEndLeft(monitor.x, monitor.y + monitor.height - BAR_HEIGHT, COLOR_BAR_ENDS)
        cr.drawBarEndRight(monitor.x + monitor.width - 32, monitor.y, COLOR_BAR_ENDS)
        cr.drawBarEndRight(monitor.x + monitor.width - 32, monitor.y + monitor.height - BAR_HEIGHT, COLOR_BAR_ENDS)

        cr.drawRectangle(
            if (monitor.isPrimary)
                (monitor.x + 2 * BAR_GAP_SIZE + BAR_END_WIDTH + SIDE_BAR_WIDTH)
            else
                (monitor.x + BAR_GAP_SIZE + BAR_END_WIDTH),
            monitor.y,
            if (monitor.isPrimary)
                (monitor.width - 3 * BAR_GAP_SIZE - 2 * BAR_END_WIDTH - SIDE_BAR_WIDTH)
            else
                (monitor.width - 2 * (BAR_GAP_SIZE + BAR_END_WIDTH)),
            BAR_HEIGHT,
            COLOR_MAX_BAR_UP
        )

        cr.drawRectangle(
            monitor.x + 40,
            monitor.y + monitor.height - 40,
            monitor.width - 80,
            BAR_HEIGHT,
            COLOR_MAX_BAR_DOWN
        )
        if (logo != null) {
            drawLogo(
                cr,
                monitor.x + monitor.width - 2 * BAR_GAP_SIZE - BAR_END_WIDTH - logo.width,
                monitor.y
            )
        } else {
            cr.drawText(
                logoText,
                fontFamily,
                monitor.x + monitor.width - BAR_GAP_SIZE - BAR_END_WIDTH,
                monitor.y,
                BAR_HEIGHT,
                TextAlignment.RIGHT,
                COLOR_LOGO,
                COLOR_BACKGROUND
            )
        }
    }

    private fun drawNormalFrame(monitor: Monitor<*>, cr: Session) {
        clearScreen(monitor, cr)

        // bottom bar
        cr.drawRectangle(
            monitor.x + 320,
            monitor.y + monitor.height - 40,
            monitor.width - 320,
            40,
            COLOR_NORMAL_BAR_DOWN
        )

        val middleSegmentWidth = (monitor.width - 240) / 8
        // upper middle bars
        cr.drawRectangle(
            monitor.x + 232 + 32,
            monitor.y + BAR_HEIGHT + INNER_CORNER_RADIUS + 2 * BAR_GAP_SIZE + DATA_BAR_HEIGHT,
            (middleSegmentWidth * 6 - 32),
            16,
            COLOR_NORMAL_BAR_MIDDLE_1
        )
        cr.drawRectangle(
            monitor.x + 240 + middleSegmentWidth * 6,
            monitor.y + BAR_HEIGHT + INNER_CORNER_RADIUS + 2 * BAR_GAP_SIZE + DATA_BAR_HEIGHT,
            middleSegmentWidth * 2,
            16,
            COLOR_NORMAL_BAR_MIDDLE_2
        )
        cr.drawRectangle(
            monitor.x + 232 + 32,
            monitor.y + BAR_HEIGHT + INNER_CORNER_RADIUS + 3 * BAR_GAP_SIZE + DATA_BAR_HEIGHT + BAR_HEIGHT_SMALL,
            middleSegmentWidth * 3 - 32,
            16,
            COLOR_NORMAL_BAR_MIDDLE_3
        )
        cr.drawRectangle(
            monitor.x + 240 + middleSegmentWidth * 3,
            monitor.y + BAR_HEIGHT + INNER_CORNER_RADIUS + 3 * BAR_GAP_SIZE + DATA_BAR_HEIGHT + BAR_HEIGHT_SMALL,
            middleSegmentWidth * 5,
            16,
            COLOR_NORMAL_BAR_MIDDLE_4
        )

        cr.drawCornerUp(
            monitor.x,
            monitor.y + BAR_HEIGHT + BAR_GAP_SIZE,
            256,
            SIDE_BAR_WIDTH,
            160,
            BAR_HEIGHT_SMALL,
            OUTER_CORNER_RADIUS_SMALL,
            INNER_CORNER_RADIUS,
            COLOR_NORMAL_CORNER_1
        )

        cr.drawCornerDown(
            monitor.x,
            monitor.y + BAR_HEIGHT + 3 * BAR_GAP_SIZE + DATA_BAR_HEIGHT + 32,
            256,
            SIDE_BAR_WIDTH,
            32,
            BAR_HEIGHT_SMALL,
            OUTER_CORNER_RADIUS_SMALL,
            INNER_CORNER_RADIUS,
            COLOR_NORMAL_CORNER_2
        )

        cr.drawRectangle(
            monitor.x,
            monitor.y + NORMAL_WINDOW_UPPER_OFFSET + INNER_CORNER_RADIUS,
            SIDE_BAR_WIDTH,
            monitor.height - NORMAL_WINDOW_NON_APP_HEIGHT,
            COLOR_NORMAL_SIDEBAR_DOWN
        )

        cr.drawCornerUp(
            monitor.x,
            monitor.y + monitor.height - BAR_HEIGHT - INNER_CORNER_RADIUS,
            312,
            SIDE_BAR_WIDTH,
            BAR_HEIGHT + INNER_CORNER_RADIUS,
            BAR_HEIGHT,
            OUTER_CORNER_RADIUS_BIG,
            INNER_CORNER_RADIUS,
            COLOR_NORMAL_CORNER_3
        )

        if (logo != null) {
            drawLogo(cr, monitor.x + monitor.width - 16 - logo.width, monitor.y)
        } else {
            cr.drawText(
                logoText,
                fontFamily,
                monitor.x + monitor.width,
                monitor.y,
                BAR_HEIGHT,
                TextAlignment.RIGHT,
                COLOR_LOGO,
                COLOR_BACKGROUND
            )
        }
    }

    private fun clearScreen(monitor: Monitor<*>, cr: Session) {
        cr.drawRectangle(
            monitor.x,
            monitor.y,
            monitor.width,
            monitor.height,
            COLOR_BACKGROUND
        )
    }
}
