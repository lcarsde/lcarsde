package de.atennert.lcarsde.menu.gtk

import de.atennert.lcarsde.menu.Uint32_t

class GtkWindow(type: GtkWindowType = GtkWindowType.TOPLEVEL) :
    GtkContainer(GTK.INSTANCE.gtk_window_new(Uint32_t(type.value))) {

    fun showAll() {
        GTK.INSTANCE.gtk_widget_show_all(widget)
    }

    fun setUtf8Property(name: String, value: String) {
        val gdkWindow = GTK.INSTANCE.gtk_widget_get_window(widget)
        GDK.INSTANCE.gdk_x11_window_set_utf8_property(gdkWindow, name, value)
    }
}
