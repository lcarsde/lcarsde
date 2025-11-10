package de.atennert.gtk

import gtk.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret

@OptIn(ExperimentalForeignApi::class)
class GtkWindow(
    type: gtk.GtkWindowType = gtk.GtkWindowType.GTK_WINDOW_TOPLEVEL,
    private val window: CPointer<_GtkWidget>? = gtk_window_new(type)
) : GtkContainer(window) {

    fun setUtf8Property(name: String, value: String) {
        val gdkWindow = gtk_widget_get_window(window)
        gdk_x11_window_set_utf8_property(gdkWindow, name, value)
    }

    fun setTitle(title: String) {
        gtk_window_set_title(window?.reinterpret(), title)
    }
}
