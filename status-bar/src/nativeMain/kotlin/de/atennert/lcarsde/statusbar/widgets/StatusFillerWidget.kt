package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import kotlinx.cinterop.*
import statusbar.*
import kotlin.math.PI
import kotlin.random.Random

// ps -U andi -o pid,pcpu,comm

@ExperimentalForeignApi
class StatusFillerWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, null) {

    private var ref: StableRef<StatusFillerWidget>? = null

    private var lastColor = "123"

    init {
        widget = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
    }

    override fun start() {
        ref = StableRef.create(this)

        gSignalConnect(widget, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t>, p: COpaquePointer -> draw(c, p) },
            ref!!.asCPointer())

        super.start()

        update()
    }

    override fun stop() {
        super.stop()

        ref!!.dispose()
    }

    override fun update() {
        gtk_widget_queue_draw(widget)
    }

    companion object {
        private val colors = arrayOf("c9c", "99c", "f96", "000")

        private fun draw(context: CPointer<cairo_t>, ref: COpaquePointer) {
            val widget = ref.asStableRef<StatusFillerWidget>().get()

            val availableColors = colors.filterNot { it == widget.lastColor }
            val colorIdx = Random.nextInt(availableColors.size)
            val newColor = availableColors[colorIdx]
            val text = "${Random.nextInt(10000)}".padStart(4, '0')
            widget.lastColor = newColor

            cairo_set_source_rgb(context, convertColor(newColor[0]), convertColor(newColor[1]), convertColor(newColor[2]))
            createBorderPath(context, widget)
            cairo_fill(context)

            drawText(context, widget, text)
        }

        private fun convertColor(char: Char): Double {
            return when(char) {
                'f' -> 1.0
                'c' -> 0.8
                '9' -> 0.6
                '6' -> 0.4
                else -> 0.0
            }
        }

        private fun createBorderPath(context: CPointer<cairo_t>, widget: StatusWidget) {
            cairo_arc(context, 20.0, 20.0, 20.0, 1.0 * PI, 1.5 * PI)
            cairo_line_to(context, widget.widthPx - 20.0, 0.0)
            cairo_arc(context, widget.widthPx - 20.0, 20.0, 20.0, 1.5 * PI, 2.0 * PI)
            cairo_line_to(context, widget.widthPx.toDouble(), widget.heightPx - 20.0)
            cairo_arc(context, widget.widthPx - 20.0, widget.heightPx - 20.0, 20.0, 0.0 * PI, 0.5 * PI)
            cairo_line_to(context, 20.0, widget.heightPx.toDouble())
            cairo_arc(context, 20.0, widget.heightPx - 20.0, 20.0, 0.5 * PI, 1.0 * PI)
            cairo_close_path(context)
        }

        private fun drawText(context: CPointer<cairo_t>, widget: StatusFillerWidget, text: String) {
            cairo_set_source_rgb(context, 0.0, 0.0, 0.0)
            val layout = pango_cairo_create_layout(context)!!

            val fontDescription = pango_font_description_from_string("Ubuntu Condensed, 12")
            pango_layout_set_font_description(layout, fontDescription)
            pango_layout_set_text(layout, text, text.length)

            val layoutSize = IntArray(2)
            pango_layout_get_size(layout, layoutSize.refTo(0), layoutSize.refTo(1))
            cairo_move_to(context,
                widget.widthPx - (layoutSize[0].toFloat() / 1024.0) - 16,
                widget.heightPx - (layoutSize[1].toFloat() / 1024.0)
            )

            pango_cairo_show_layout(context, layout)
            pango_font_description_free(fontDescription)
            g_object_unref(layout)
        }
    }
}