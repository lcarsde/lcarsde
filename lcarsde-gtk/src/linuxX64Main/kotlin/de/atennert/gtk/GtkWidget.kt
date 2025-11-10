package de.atennert.gtk

import gtk.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
open class GtkWidget(val widget: CPointer<_GtkWidget>?) {
    fun setStyling(cssProvider: CssProvider, vararg classes: String) {
        val styleContext = gtk_widget_get_style_context(widget)
        for (cssClass in classes) {
            gtk_style_context_add_class(styleContext, cssClass)
        }
        gtk_style_context_add_provider(
            styleContext,
            cssProvider.ref?.reinterpret(),
            GTK_STYLE_PROVIDER_PRIORITY_USER.convert()
        )
    }

    fun addClass(cssClass: String) {
        val styleContext = gtk_widget_get_style_context(widget)
        gtk_style_context_add_class(styleContext, cssClass)
        this.showAll()
    }

    fun removeClass(cssClass: String) {
        val styleContext = gtk_widget_get_style_context(widget)
        gtk_style_context_remove_class(styleContext, cssClass)
        this.showAll()
    }

    fun hasClass(cssClass: String): Boolean {
        val styleContext = gtk_widget_get_style_context(widget)
        return gtk_style_context_has_class(styleContext, cssClass) != 0
    }

    fun setSize(width: Int, height: Int) {
        gtk_widget_set_size_request(widget, width, height)
    }

    fun showAll() {
        gtk_widget_show_all(widget)
    }

    class CallbackDataContainer<T>(
        val callback: (CPointer<_GtkWidget>, T) -> Unit,
        val data: T
    ) {
        fun exec(ptr: CPointer<_GtkWidget>) = callback(ptr, data)
    }

    inline fun <reified T : Any> connect(
        signal: String,
        data: T,
        noinline callback: (CPointer<_GtkWidget>, T) -> Unit
    ) {
        val container = CallbackDataContainer(callback, data)
        g_signal_connect_data(
            widget,
            signal,
            (staticCFunction { w: CPointer<_GtkWidget>, c: COpaquePointer ->
                val container = c.asStableRef<CallbackDataContainer<T>>().get()
                container.exec(w)
            }).reinterpret(),
            StableRef.create(container).asCPointer(),
            null,
            0u
        )
    }

    class CallbackContainer(
        val callback: (CPointer<_GtkWidget>) -> Unit
    ) {
        fun exec(ptr: CPointer<_GtkWidget>) = callback(ptr)
    }

    fun connect(signal: String, callback: (CPointer<_GtkWidget>) -> Unit) {
        val container = CallbackContainer(callback)
        g_signal_connect_data(widget, signal, (staticCFunction { w: CPointer<_GtkWidget>, c: COpaquePointer ->
            val container = c.asStableRef<CallbackContainer>().get()
            container.exec(w)
        }).reinterpret(), StableRef.create(container).asCPointer(), null, 0u)
    }

    fun setHAlign(hAlign: GtkAlign) {
        gtk_widget_set_halign(widget, hAlign)
    }

    fun setVAlign(vAlign: GtkAlign) {
        gtk_widget_set_valign(widget, vAlign)
    }

    fun setAlign(hAlign: GtkAlign, vAlign: GtkAlign) {
        setHAlign(hAlign)
        setVAlign(vAlign)
    }
}
