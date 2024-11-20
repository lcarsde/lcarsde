package de.atennert.lcarsde.menu.gtk

import com.sun.jna.Pointer
import de.atennert.lcarsde.menu.Uint32_t

open class GtkWidget(val widget: Pointer) {
    fun setStyling(cssProvider: Pointer, vararg classes: String) {
        val styleContext = GTK.INSTANCE.gtk_widget_get_style_context(widget)
        for (cssClass in classes) {
            GTK.INSTANCE.gtk_style_context_add_class(styleContext, cssClass)
        }
        GTK.INSTANCE.gtk_style_context_add_provider(
            styleContext,
            cssProvider,
            Uint32_t(GtkStyleProviderPriority.USER.value)
        )
    }

    fun addClass(cssClass: String) {
        val styleContext = GTK.INSTANCE.gtk_widget_get_style_context(widget)
        GTK.INSTANCE.gtk_style_context_add_class(styleContext, cssClass)
    }

    fun removeClass(cssClass: String) {
        val styleContext = GTK.INSTANCE.gtk_widget_get_style_context(widget)
        GTK.INSTANCE.gtk_style_context_remove_class(styleContext, cssClass)
    }

    fun setSize(width: Int, height: Int) {
        GTK.INSTANCE.gtk_widget_set_size_request(widget, width, height)
    }

    fun connect(signal: String, callback: SignalCallback) {
        GObject.INSTANCE.g_signal_connect_data(
            widget,
            signal,
            callback,
            null,
            null,
            Uint32_t(0)
        )
    }
}