package de.atennert.gtk

import de.atennert.Uint32_t

class GtkWindow(type: GtkWindowType = GtkWindowType.TOPLEVEL) :
    GtkContainer(GTK.INSTANCE.gtk_window_new(Uint32_t(type.value))) {

    fun setUtf8Property(name: String, value: String) {
        val gdkWindow = GTK.INSTANCE.gtk_widget_get_window(widget)
        GDK.INSTANCE.gdk_x11_window_set_utf8_property(gdkWindow, name, value)
    }

    fun setTitle(title: String) {
        GTK.INSTANCE.gtk_window_set_title(widget, title)
    }
}
