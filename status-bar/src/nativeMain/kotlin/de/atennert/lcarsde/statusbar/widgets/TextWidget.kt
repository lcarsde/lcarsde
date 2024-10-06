package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import kotlinx.cinterop.*
import statusbar.*

/**
 * TextWidget is an abstract class that acts as a frame for widgets
 * that display one short line of text. This widget draws text without its
 * font ascent and descent area!
 *
 * To use: extend this class and override the create_text method.
 */
@ExperimentalForeignApi
abstract class TextWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>, updateDelayMs: Long)
    : StatusWidget(widgetConfiguration, cssProvider, updateDelayMs) {

    private var ref: StableRef<TextWidget>? = null

    init {
        widget = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
    }

    override fun start() {
        ref = StableRef.create(this)

        gSignalConnect(widget, "draw",
                staticCFunction { w: CPointer<GtkWidget>, c: CPointer<cairo_t>, p: COpaquePointer -> drawText(w, c, p) },
                ref!!.asCPointer())

        super.start()
    }

    override fun stop() {
        super.stop()

        ref!!.dispose()
    }

    override fun update() {
        gtk_widget_queue_draw(widget)
    }

    abstract fun getText(): String

    companion object {
        fun drawText(widget: CPointer<GtkWidget>, context: CPointer<cairo_t>, pWidget: COpaquePointer) {
            val text = pWidget.asStableRef<TextWidget>().get().getText()
            val width = gtk_widget_get_allocated_width(widget)

            cairo_set_source_rgb(context, 1.0, 0.6, 0.0)
            val layout = pango_cairo_create_layout(context)!!

            val fontDescription = pango_font_description_from_string("Ubuntu Condensed, 40")
            pango_layout_set_font_description(layout, fontDescription)
            pango_layout_set_text(layout, text, text.length)

            val layoutWidth = IntArray(1)
            pango_layout_get_size(layout, layoutWidth.refTo(0), null)
            cairo_move_to(context, width - (layoutWidth[0].toFloat() / 1024.0), -11.0)

            pango_cairo_show_layout(context, layout)
            pango_font_description_free(fontDescription)
            g_object_unref(layout)
        }
    }
}