package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import kotlinx.cinterop.*
import statusbar.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@ExperimentalForeignApi
abstract class RadarGraphWidget(widgetConfiguration: WidgetConfiguration,
                                cssProvider: CPointer<GtkCssProvider>,
                                updateDelayMs: Long?,
                                maxScale: Int)
    : StatusWidget(widgetConfiguration, cssProvider, updateDelayMs) {

    protected val cx = widthPx.toDouble() / 2
    protected val cy = heightPx.toDouble() / 2
    protected val minDimension = min(cx, cy)
    private val scale = minDimension / maxScale
    protected val scaledMax = maxScale * scale

    protected abstract val attentionValue: Double
    protected abstract val warningValue: Double

    private var ref: StableRef<RadarGraphWidget>? = null

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

    abstract fun getData(): List<Double>

    fun drawData(context: CPointer<cairo_t>) {
        val values = getData()

        val maxFreq = values.maxOrNull() ?: return
        val points = ArrayList<Pair<Double, Double>>(values.size)
        var angle = 0.0
        for (value in values) {
            points.add(polarToCartesian(this, value * scale, angle))
            angle += 360 / values.size
        }

        setColor(maxFreq, context)

        drawPoints(context, points)
    }

    private fun setColor(maxValue: Double, context: CPointer<cairo_t>) {
        when {
            maxValue > warningValue -> setDoubleRgb(context, 0.8, 0.4, 0.4)
            maxValue > attentionValue -> setDoubleRgb(context, 1.0, 0.6, 0.0)
            else -> setDoubleRgb(context, 1.0, 0.8, 0.6)
        }
    }

    private fun setDoubleRgb(context: CPointer<cairo_t>, r: Double, g: Double, b: Double) {
        cairo_set_source_rgb(context, r, g, b)
        cairo_set_source_rgba(context, r, g, b, 0.6)
    }

    private fun drawPoints(context: CPointer<cairo_t>, points: List<Pair<Double, Double>>) {
        val (x1, y1) = points[0]
        cairo_move_to(context, x1, y1)
        for (i in 1 until points.size) {
            val (x, y) = points[i]
            cairo_line_to(context, x, y)
        }
        cairo_close_path(context)
        cairo_fill_preserve(context)
        cairo_stroke(context)
    }

    companion object {
        protected fun draw(context: CPointer<cairo_t>, pWidget: COpaquePointer) {
            val radarWidget = pWidget.asStableRef<RadarGraphWidget>().get()

            drawRadarCross(context, radarWidget)
            radarWidget.drawData(context)
        }

        private fun drawRadarCross(context: CPointer<cairo_t>, radar: RadarGraphWidget) {
            cairo_set_source_rgb(context, 0.6, 0.6, 0.8)

            cairo_move_to(context, 0.0, radar.cy)
            cairo_line_to(context, radar.widthPx.toDouble(), radar.cy)
            cairo_move_to(context, radar.cx, 0.0)
            cairo_line_to(context, radar.cx, radar.heightPx.toDouble())
            val (v1, v2) = polarToCartesian(radar, radar.minDimension, 135.0)
            val (mi, ma) = if (v1 < v2) Pair(v1, v2) else Pair(v2, v1)
            cairo_move_to(context, mi, mi)
            cairo_line_to(context, ma, ma)
            cairo_move_to(context, mi, ma)
            cairo_line_to(context, ma, mi)
            cairo_stroke(context)

            cairo_arc(context, radar.cx, radar.cy, .8 * radar.scaledMax, 0.0, 2.0 * PI)
            cairo_stroke(context)
            cairo_arc(context, radar.cx, radar.cy, .4 * radar.scaledMax, 0.0, 2.0 * PI)
            cairo_stroke(context)
        }

        protected fun polarToCartesian(radar: RadarGraphWidget, radius: Double, angle: Double): Pair<Double, Double> {
            val radAngle = angle * PI / 180
            return Pair(
                    radius * cos(radAngle) + radar.cx,
                    radius * sin(radAngle) + radar.cy
            )
        }
    }
}