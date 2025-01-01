package de.atennert.gtk

import com.sun.jna.Pointer
import de.atennert.Uint32_t

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
        this.showAll()
    }

    fun removeClass(cssClass: String) {
        val styleContext = GTK.INSTANCE.gtk_widget_get_style_context(widget)
        GTK.INSTANCE.gtk_style_context_remove_class(styleContext, cssClass)
        this.showAll()
    }

    fun hasClass(cssClass: String): Boolean {
        val styleContext = GTK.INSTANCE.gtk_widget_get_style_context(widget)
        return GTK.INSTANCE.gtk_style_context_has_class(styleContext, cssClass)
    }

    fun setSize(width: Int, height: Int) {
        GTK.INSTANCE.gtk_widget_set_size_request(widget, width, height)
    }

    fun showAll() {
        GTK.INSTANCE.gtk_widget_show_all(widget)
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

    fun setHAlign(hAlign: GtkAlignment) {
        GTK.INSTANCE.gtk_widget_set_halign(widget, hAlign.value)
    }

    fun setVAlign(vAlign: GtkAlignment) {
        GTK.INSTANCE.gtk_widget_set_valign(widget, vAlign.value)
    }

    fun setAlign(hAlign: GtkAlignment, vAlign: GtkAlignment) {
        setHAlign(hAlign)
        setVAlign(vAlign)
    }
}
