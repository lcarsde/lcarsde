package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.BAR_HEIGHT
import de.atennert.lcarswm.COLOR_LOGO
import kotlinx.cinterop.*
import platform.posix.M_PI
import xlib.*

private object Arc {
    val `0` = 0.0
    val `90` = 90 * (M_PI / 180)
    val `180` = 180 * (M_PI / 180)
    val `270` = 270 * (M_PI / 180)
}

@OptIn(ExperimentalForeignApi::class)
object CairoDrawFactory : DrawFactory {
    override fun createSurface(configuration: Surface.Configuration): Surface {
        return CairoSurface(
            configuration.display,
            configuration.drawable,
            configuration.visual,
            configuration.width,
            configuration.height
        )
    }

    override fun createImageFromFile(path: String) = CairoImage(path)
}

@OptIn(ExperimentalForeignApi::class)
class CairoSession(internal val cr: CPointer<cairo_t>?) : Session {
    override fun drawImage(image: Image, x: Int, y: Int) {
        if (image !is CairoImage) throw NotImplementedError("Only CairoImage is supported")

        cairo_set_source_surface(cr, image.cairoImageSurface, x.toDouble() + 8, y.toDouble())
        cairo_paint(cr)
    }

    override fun drawText(
        text: String,
        font: String?,
        x: Int,
        y: Int,
        height: Int,
        alignment: TextAlignment,
        textColor: Color,
        bgColor: Color?
    ) {
        val imageBorderX = if (bgColor == null) 0 else 8

        cairo_select_font_face(
            cr,
            font,
            _cairo_font_slant.CAIRO_FONT_SLANT_NORMAL,
            _cairo_font_weight.CAIRO_FONT_WEIGHT_BOLD
        )
        // approximate target size
        cairo_set_font_size(cr, height.toDouble())

        val extents = nativeHeap.alloc<cairo_text_extents_t>()
        cairo_text_extents(cr, text, extents.ptr)

        // scale to make the font size fit the exact bar height
        val scaleFactor = height / extents.height
        cairo_set_font_size(cr, height * scaleFactor)

        cairo_text_extents(cr, text, extents.ptr)

        val (textX, bgX) = if (alignment == TextAlignment.LEFT) {
            Pair(x + imageBorderX, x)
        } else {
            Pair(x - extents.width.toInt() - imageBorderX, x - extents.width.toInt() - 2 * imageBorderX)
        }

        if (bgColor != null) {
            drawRectangle(
                bgX,
                y,
                extents.width.toInt() + 2 * imageBorderX,
                height,
                bgColor
            )
        }

        cairo_set_source_color(cr, COLOR_LOGO)
        cairo_move_to(cr, textX.toDouble(), y + height.toDouble())
        cairo_show_text(cr, text)
    }

    override fun drawBarEndLeft(x: Double, y: Double, color: Color) {
        val halfBarHeight = BAR_HEIGHT / 2.0
        val rectWidth = 12.0

        cairo_set_source_color(cr, color)
        cairo_move_to(cr, x + halfBarHeight + rectWidth, y)
        cairo_rel_line_to(cr, 0.0, BAR_HEIGHT.toDouble())
        cairo_rel_line_to(cr, -rectWidth, 0.0)
        cairo_arc(cr, x + halfBarHeight, y + halfBarHeight, halfBarHeight, Arc.`90`, Arc.`270`)
        cairo_close_path(cr)
        cairo_fill(cr)
    }

    override fun drawBarEndRight(x: Double, y: Double, color: Color) {
        val halfBarHeight = BAR_HEIGHT / 2.0
        val rectWidth = 12.0

        cairo_set_source_color(cr, color)
        cairo_move_to(cr, x + rectWidth, y)
        cairo_rel_line_to(cr, -rectWidth, 0.0)
        cairo_rel_line_to(cr, 0.0, BAR_HEIGHT.toDouble())
        cairo_rel_line_to(cr, rectWidth, 0.0)
        cairo_arc(cr, x + rectWidth, y + halfBarHeight, halfBarHeight, Arc.`270`, Arc.`90`)
        cairo_close_path(cr)
        cairo_fill(cr)
    }

    override fun drawRectangle(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color
    ) {
        cairo_set_source_color(cr, color)
        cairo_rectangle(cr, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        cairo_fill(cr)
    }

    override fun drawCornerUp(
        x: Int,
        y: Int,
        width: Int,
        sideWidth: Int,
        height: Int,
        barHeight: Int,
        rOuter: Int,
        rInner: Int,
        color: Color
    ) {
        cairo_set_source_color(cr, color)

        cairo_move_to(cr, x.toDouble(), y.toDouble())
        cairo_rel_line_to(cr, sideWidth.toDouble(), 0.0)
        cairo_rel_line_to(cr, 0.0, height - rInner - barHeight.toDouble())
        cairo_arc_negative(
            cr,
            x + sideWidth + rInner.toDouble(),
            y + height - rInner - barHeight.toDouble(),
            rInner.toDouble(),
            Arc.`180`,
            Arc.`90`
        )
        cairo_rel_line_to(cr, width - sideWidth - rInner.toDouble(), 0.0)
        cairo_rel_line_to(cr, 0.0, barHeight.toDouble())
        cairo_rel_line_to(cr, rOuter - width.toDouble(), 0.0)
        cairo_arc(
            cr,
            x + rOuter.toDouble(),
            y + height - rOuter.toDouble(),
            rOuter.toDouble(),
            Arc.`90`,
            Arc.`180`
        )
        cairo_close_path(cr)

        cairo_fill(cr)
    }

    override fun drawCornerDown(
        x: Int,
        y: Int,
        width: Int,
        sideWidth: Int,
        height: Int,
        barHeight: Int,
        rOuter: Int,
        rInner: Int,
        color: Color
    ) {
        cairo_set_source_color(cr, color)

        cairo_move_to(cr, x.toDouble(), y + height.toDouble())
        cairo_rel_line_to(cr, sideWidth.toDouble(), 0.0)
        cairo_rel_line_to(cr, 0.0, rInner + barHeight - height.toDouble())
        cairo_arc(
            cr,
            x + sideWidth + rInner.toDouble(),
            y + rInner + barHeight.toDouble(),
            rInner.toDouble(),
            Arc.`180`,
            Arc.`270`
        )
        cairo_rel_line_to(cr, width - sideWidth - rInner.toDouble(), 0.0)
        cairo_rel_line_to(cr, 0.0, -barHeight.toDouble())
        cairo_rel_line_to(cr, rOuter - width.toDouble(), 0.0)
        cairo_arc_negative(
            cr,
            x + rOuter.toDouble(),
            y + rOuter.toDouble(),
            rOuter.toDouble(),
            Arc.`270`,
            Arc.`180`
        )
        cairo_close_path(cr)

        cairo_fill(cr)
    }

    override fun dispose() = cairo_destroy(cr)
}

@OptIn(ExperimentalForeignApi::class)
class CairoImage(imagePath: String) : Image {
    internal val cairoImageSurface by lazy { cairo_image_surface_create_from_png(imagePath) }

    override val width by lazy { cairo_image_surface_get_width(cairoImageSurface) }

    override fun dispose() = cairo_surface_destroy(cairoImageSurface)
}

@OptIn(ExperimentalForeignApi::class)
class CairoSurface(
    display: CValuesRef<Display>?,
    drawable: Drawable,
    visual: CValuesRef<Visual>?,
    width: Int,
    height: Int
) : Surface {
    private val cairoSurface by lazy {
        cairo_xlib_surface_create(
            display,
            drawable,
            visual,
            width,
            height
        )
    }

    override fun setSize(width: Int, height: Int) {
        cairo_xlib_surface_set_size(cairoSurface, width, height)
    }

    override fun createSession() = CairoSession(cairo_create(cairoSurface))

    override fun dispose() = cairo_surface_destroy(cairoSurface)
}

@OptIn(ExperimentalForeignApi::class)
private fun cairo_set_source_color(cr: CPointer<cairo_t>?, color: Color) {
    val (r, g, b) = color.base1
    cairo_set_source_rgb(cr, r, g, b)
}
