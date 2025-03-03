package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import gtk.GtkCssProvider
import gtk.g_date_time_format
import gtk.g_date_time_new_now_local

/**
 * Displays the current time
 */
@ExperimentalForeignApi
class DateWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : TextWidget(widgetConfiguration, cssProvider, 1000) {

    override fun getText(): String {
        val currentTime = g_date_time_new_now_local()
        return g_date_time_format(currentTime, "%d.%m.%y")?.toKString() ?: "--:--:--"
    }
}