package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.*
import statusbar.*
import kotlin.math.PI

@ExperimentalForeignApi
class EthernetWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>) :
    StatusWidget(widgetConfiguration, cssProvider, 1000) {

    private var ref: StableRef<EthernetWidget>? = null

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
            val widget = ref.asStableRef<EthernetWidget>().get()
            val status = readData(widget)

            drawConnector(context, status)
            drawEthStatus(context, status)
        }

        private fun readData(widget: EthernetWidget): String {
            val devicePath = "/sys/class/net/${widget.properties["device"]}"

            opendir(devicePath)?.let {
                closedir(it)

                val status = readFile("$devicePath/operstate") ?: return "Unavailable"

                return if (status == "up") "Up" else "Down"
            }
            return "Unavailable"
        }

        private fun drawConnector(context: CPointer<cairo_t>, status: String) {
            when (status) {
                "Unavailable" -> cairo_set_source_rgb(context, 0.8, 0.4, 0.4)
                "Down" -> cairo_set_source_rgb(context, 0.6, 0.6, 0.8)
                else -> cairo_set_source_rgb(context, 1.0, 0.8, 0.6)
            }

            // cable
            cairo_rectangle(context, 0.0, 35.0, 40.0, 2.0)
            if (status == "Down") {
                cairo_rectangle(context, 19.0, 21.0, 2.0, 5.0)
            } else {
                cairo_rectangle(context, 19.0, 21.0, 2.0, 12.0)
                cairo_arc(context, 20.0, 36.0, 4.0, 0.0, 2 * PI)
            }

            cairo_rectangle(context, 13.0, 20.0, 14.0, 2.0)
            cairo_rectangle(context, 13.0, 3.0, 14.0, 2.0)
            cairo_rectangle(context, 13.0, 3.0, 2.0, 18.0)
            cairo_rectangle(context, 25.0, 3.0, 2.0, 18.0)
            cairo_rectangle(context, 16.5, 3.0, 1.0, 8.0)
            cairo_rectangle(context, 18.5, 3.0, 1.0, 8.0)
            cairo_rectangle(context, 20.5, 3.0, 1.0, 8.0)
            cairo_rectangle(context, 22.5, 3.0, 1.0, 8.0)
            cairo_rectangle(context, 18.0, 0.0, 4.0, 2.0)
            cairo_rectangle(context, 17.0, 0.0, 2.0, 4.0)
            cairo_rectangle(context, 21.0, 0.0, 2.0, 4.0)
            cairo_fill(context)
        }

        private fun drawEthStatus(context: CPointer<cairo_t>, status: String) {
            if (status == "Unavailable") {
                cairo_set_source_rgb(context, 0.8, 0.4, 0.4)

                cairo_move_to(context, 8.0, 30.0)
                cairo_line_to(context, 32.0, 2.0)
                cairo_stroke(context)
            } else if (status == "Up") {
                cairo_set_source_rgba(context, 1.0, 0.8, 0.6, 0.6)

                cairo_rectangle(context, 15.0, 5.0, 10.0, 16.0)
                cairo_rectangle(context, 19.0, 2.0, 2.0, 1.0)
                cairo_fill(context)
            }
        }
    }
}