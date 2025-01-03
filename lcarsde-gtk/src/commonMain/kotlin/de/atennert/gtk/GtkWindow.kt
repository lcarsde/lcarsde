package de.atennert.gtk

class GtkWindow(
    type: GtkWindowType = GtkWindowType.TOPLEVEL,
    private val window: WindowRef = gtkWindowNew(type)
) : GtkContainer(window.toWContainerRef()) {

    fun setUtf8Property(name: String, value: String) {
        val gdkWindow = gtkWidgetGetWindow(window)
        gdkX11WindowSetUtf8Property(gdkWindow, name, value)
    }

    fun setTitle(title: String) {
        gtkWindowSetTitle(window, title)
    }
}
