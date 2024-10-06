package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.*
import statusbar.*
import kotlin.math.PI

@ExperimentalForeignApi
class WifiWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>) :
    StatusWidget(widgetConfiguration, cssProvider, 1000) {

    private var ref: StableRef<WifiWidget>? = null

    init {
        widget = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
    }

    override fun start() {
        ref = StableRef.create(this)

        gSignalConnect(
            widget, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t>, p: COpaquePointer -> draw(c, p) },
            ref!!.asCPointer()
        )

        super.start()
    }

    override fun stop() {
        super.stop()

        ref!!.dispose()
    }

    override fun update() {
        gtk_widget_queue_draw(widget)
    }

    companion object {
        private fun draw(context: CPointer<cairo_t>, ref: COpaquePointer) {
            val widget = ref.asStableRef<WifiWidget>().get()
            val status = readData(widget)

            drawAntenna(context, status)
            drawWifiStatus(context, status)
        }

        private fun readData(widget: WifiWidget): String {
            val devicePath = "/sys/class/net/${widget.properties["device"]}"

            opendir(devicePath)?.let {
                closedir(it)

                val status = readFile("$devicePath/operstate") ?: return "Unavailable"

                return if (status == "up") "Up" else "Down"
            }
            return "Unavailable"
        }

        private fun drawAntenna(context: CPointer<cairo_t>, status: String) {
            when (status) {
                "Unavailable" -> cairo_set_source_rgb(context, 0.8, 0.4, 0.4)
                "Down" -> cairo_set_source_rgb(context, 0.6, 0.6, 0.8)
                else -> cairo_set_source_rgb(context, 1.0, 0.8, 0.6)
            }

            cairo_rectangle(context, 19.0, 14.0, 2.0, 25.0)
            cairo_arc(context, 20.0, 14.0, 4.0, 0.0, 2 * PI)
            cairo_fill(context)
        }

        private fun drawWifiStatus(context: CPointer<cairo_t>, status: String) {
            if (status == "Unavailable") {
                cairo_set_source_rgb(context, 0.8, 0.4, 0.4)

                cairo_move_to(context, 13.0, 39.0)
                cairo_line_to(context, 27.0, 14.0)
                cairo_stroke(context)
            } else if (status == "Up") {
                cairo_set_source_rgba(context, 1.0, 0.8, 0.6, 0.6)

                // left side
                cairo_arc(context, 20.0, 14.0, 7.0, 0.6 * PI, 1.4 * PI)
                cairo_stroke(context)
                cairo_arc(context, 20.0, 14.0, 10.0, 0.6 * PI, 1.4 * PI)
                cairo_stroke(context)
                cairo_arc(context, 20.0, 14.0, 13.0, 0.6 * PI, 1.4 * PI)
                cairo_stroke(context)

                // right side
                cairo_arc(context, 20.0, 14.0, 7.0, 1.6 * PI, 0.4 * PI)
                cairo_stroke(context)
                cairo_arc(context, 20.0, 14.0, 10.0, 1.6 * PI, 0.4 * PI)
                cairo_stroke(context)
                cairo_arc(context, 20.0, 14.0, 13.0, 1.6 * PI, 0.4 * PI)
                cairo_stroke(context)
            }
        }
    }
}