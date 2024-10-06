package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.print
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import statusbar.*

@ExperimentalForeignApi
class StardateWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : TextWidget(widgetConfiguration, cssProvider, 10000) {

    override fun getText(): String = getStarDate().print(1)

    private fun getStarDate() : Float {
        val currentTime = g_date_time_new_now_utc()
        val year = g_date_time_get_year(currentTime)
        val hours = g_date_time_get_hour(currentTime)
        val minutes = g_date_time_get_minute(currentTime)
        val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365
        val day = g_date_time_get_day_of_year(currentTime)
        val earthTime = year + (day - 1f + hours / 24f + minutes / 1440f) / daysInYear
        return 1000 * (earthTime - 2323)
    }
}