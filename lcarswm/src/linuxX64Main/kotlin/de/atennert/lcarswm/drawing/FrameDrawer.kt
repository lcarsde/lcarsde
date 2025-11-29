package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.*
import de.atennert.lcarswm.system.api.DrawApi
import de.atennert.lcarswm.system.api.FontApi
import de.atennert.lcarswm.window.WindowMeasurements
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
class FrameDrawer(
    private val fontApi: FontApi,
    private val drawApi: DrawApi,
    private val fontProvider: FontProvider,
    private val colorFactory: ColorFactory,
    private val screen: Screen
) : IFrameDrawer {
    private val barEndOpacities = getFilledArcOpacities(BAR_HEIGHT / 2)
    private val barEndLeftColors = getArcs(COLOR_BAR_ENDS, barEndOpacities, BAR_HEIGHT / 2, 2, 3)
    private val barEndRightColors = getArcs(COLOR_BAR_ENDS, barEndOpacities, BAR_HEIGHT / 2, 1, 4)
    private val activeTextColor = colorFactory.createXftColor(COLOR_ACTIVE_TITLE)
    private val inactiveTextColor = colorFactory.createXftColor(COLOR_INACTIVE_TITLE)
    private val maxBarColor = colorFactory.createXftColor(COLOR_MAX_BAR_DOWN)
    private val normalBarColor = colorFactory.createXftColor(COLOR_NORMAL_BAR_DOWN)
    private val normalCornerDownColor = colorFactory.createXftColor(COLOR_NORMAL_CORNER_3)
    private val backgroundColor = colorFactory.createXftColor(COLOR_BACKGROUND)
    override val colorMap: Colormap
            get() = colorFactory.colorMapId

    override fun drawFrame(measurements: WindowMeasurements, screenMode: ScreenMode, isFocused: Boolean, title: String, titleBar: Window) {
        val textW = if (screenMode == ScreenMode.NORMAL) {
            measurements.width + NORMAL_WINDOW_LEFT_OFFSET - LOWER_CORNER_WIDTH - BAR_GAP_SIZE - WINDOW_TITLE_OFFSET - BAR_GAP_SIZE /* <text> */ - BAR_GAP_SIZE - BAR_END_WIDTH
        } else {
            measurements.width- BAR_END_WIDTH - BAR_GAP_SIZE - WINDOW_TITLE_OFFSET - BAR_GAP_SIZE /* <text> */ - BAR_GAP_SIZE - BAR_END_WIDTH
        }
        val textH = BAR_HEIGHT_WITH_OFFSET
        val rect = nativeHeap.alloc<PangoRectangle>()
        val (ascent, descent) = fontProvider.getAscDsc(WINDOW_TITLE_FONT_SIZE, PANGO_WEIGHT_BOLD)

        val textY = (((textH * PANGO_SCALE)
                - (ascent + descent))
                / 2 + ascent) / PANGO_SCALE

        val pixmap = drawApi.createPixmap(screen.root, measurements.width.convert(), textH.convert(), screen.root_depth.convert())
        val xftDraw = drawApi.xftDrawCreate(pixmap, screen.root_visual!!, colorMap)

        val textColor = if (isFocused) {
            activeTextColor
        } else {
            inactiveTextColor
        }

        fontApi.setLayoutText(fontProvider.layout, title)
        fontApi.setLayoutWidth(fontProvider.layout, textW * PANGO_SCALE)

        fontApi.getLayoutPixelExtents(fontProvider.layout, rect.ptr)
        val textX = if (screenMode == ScreenMode.NORMAL)
            measurements.width - rect.width
        else
            measurements.width - rect.width - BAR_END_WIDTH - BAR_GAP_SIZE

        drawApi.xftDrawRect(xftDraw, backgroundColor.ptr, 0, 0,  measurements.width.convert(), textH.convert())

        val line = fontApi.getLayoutLineReadonly(fontProvider.layout, 0)
        fontApi.xftRenderLayoutLine(xftDraw, textColor.ptr, line, textX * PANGO_SCALE, textY * PANGO_SCALE)

        if (screenMode == ScreenMode.NORMAL) {
            val primBarWidth = LOWER_CORNER_WIDTH - NORMAL_WINDOW_LEFT_OFFSET
            val secBarWidth = textX - primBarWidth - 2 * BAR_GAP_SIZE + FIRST_LETTER_OFFSET
            drawApi.xftDrawRect(xftDraw, normalCornerDownColor.ptr, 0, TITLE_BAR_OFFSET,  primBarWidth.convert(), BAR_HEIGHT.convert())
            drawApi.xftDrawRect(xftDraw, normalBarColor.ptr, primBarWidth + BAR_GAP_SIZE, TITLE_BAR_OFFSET,  secBarWidth.convert(), BAR_HEIGHT.convert())
        } else {
            val barWidth = textX - BAR_GAP_SIZE + FIRST_LETTER_OFFSET - (BAR_END_WIDTH + BAR_GAP_SIZE)
            drawApi.xftDrawRect(xftDraw, maxBarColor.ptr, BAR_END_WIDTH + BAR_GAP_SIZE, TITLE_BAR_OFFSET,  barWidth.convert(), BAR_HEIGHT.convert())

            for ((x, y, color) in barEndLeftColors) {
                val gc = colorFactory.createColorGC(screen.root, color)
                drawApi.drawPoint(pixmap, gc, x, y + 1)
            }
            for ((x, y, color) in barEndRightColors) {
                val gc = colorFactory.createColorGC(screen.root, color)
                drawApi.drawPoint(pixmap, gc, measurements.width - 40 + x, y + 1)
            }
            val barEndsGC = colorFactory.createColorGC(screen.root, COLOR_BAR_ENDS)
            val rects = nativeHeap.allocArray<XRectangle>(2)
            // extensions for round pieces
            for (i in 0 until 2) {
                rects[i].width = 12.convert()
                rects[i].height = 40.convert()
                rects[i].y = 1
            }
            rects[0].x = 20
            rects[1].x = (measurements.width - 32).convert()

            drawApi.fillRectangles(pixmap, barEndsGC, rects, 2)
        }

        drawApi.setWindowBackgroundPixmap(titleBar, pixmap)
        drawApi.clearWindow(titleBar)
        drawApi.freePixmap(pixmap)
        nativeHeap.free(rect.rawPtr)
    }

    companion object {
        private const val FIRST_LETTER_OFFSET = 2
    }
}