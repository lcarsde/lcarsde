package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.*
import statusbar.*
import kotlin.math.max

@ExperimentalForeignApi
class BatteryWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, 5000) {

    private var ref: StableRef<BatteryWidget>? = null

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
            val widget = ref.asStableRef<BatteryWidget>().get()
            val data = readData(widget)

            if (data == null) {
                drawBattery(context, 0.8, 0.4, 0.4)
            } else {
                drawBattery(context, 1.0, 0.8, 0.6)
                drawBatteryStatus(context, data.first, data.second,
                    widget.properties.getOrElse("warningCapacity", {"10"}).toInt())
            }
        }

        private fun readData(widget: BatteryWidget): Pair<Int, String>? {
            val devicePath = "/sys/class/power_supply/${widget.properties["device"]}"

            opendir(devicePath)?.let {
                closedir(it)

                val capacity = readFile("$devicePath/capacity")?.toInt() ?: return null
                val status = readFile("$devicePath/status") ?: return null

                return Pair(capacity, status)
            }
            return null
        }

        private fun drawBattery(context: CPointer<cairo_t>, r: Double, g: Double, b: Double) {
            cairo_set_source_rgb(context, r, g, b)

            cairo_move_to(context, 15.0, 6.0)
            cairo_line_to(context, 15.0, 0.0)
            cairo_line_to(context, 25.0, 0.0)
            cairo_line_to(context, 25.0, 6.0)
            cairo_line_to(context, 29.0, 6.0)
            cairo_line_to(context, 29.0, 40.0)
            cairo_line_to(context, 11.0, 40.0)
            cairo_line_to(context, 11.0, 6.0)
            cairo_line_to(context, 15.0, 6.0)
            cairo_stroke(context)
        }

        private fun drawBatteryStatus(context: CPointer<cairo_t>, capacity: Int, status: String, warningCapacity: Int) {
            when (status) {
                "Charging" -> cairo_set_source_rgba(context, 0.6, 0.6, 1.0, 0.6)
                "Discharging" -> if (capacity <= warningCapacity) {
                    cairo_set_source_rgba(context, 0.8, 0.4, 0.4, 0.6)
                } else {
                    cairo_set_source_rgba(context, 1.0, 0.6, 0.4, 0.6)
                }
                else -> cairo_set_source_rgba(context, 1.0, 0.8, 0.6, 0.6)
            }

            val capacityHeight = capacity * 38.0 / 100
            cairo_rectangle(context, 12.0, 39.0, 16.0, max(-capacityHeight, -33.0))
            cairo_rectangle(context, 16.0, 39.0, 8.0, -capacityHeight)
            cairo_fill(context)
        }
    }
}