package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.setStyling
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import statusbar.GtkCssProvider
import statusbar.gtk_label_new
import statusbar.gtk_widget_set_size_request

@ExperimentalForeignApi
class EmptyWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, null) {

    init {
        widget = gtk_label_new("")!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
        widget.setStyling(cssProvider)
    }

    override fun update() {
        // nothing to do
    }
}