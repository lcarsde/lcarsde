package de.atennert.gtk

import de.atennert.Uint32_t

open class GtkBox(orientation: GtkOrientation, spacing: Int) :
    GtkContainer(GTK.INSTANCE.gtk_box_new(Uint32_t(orientation.value), spacing)) {

    fun packStart(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        GTK.INSTANCE.gtk_box_pack_start(
            widget,
            child.widget,
            expand,
            fill,
            Uint32_t(padding.toInt())
        )
    }

    fun packEnd(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        GTK.INSTANCE.gtk_box_pack_end(
            widget,
            child.widget,
            expand,
            fill,
            Uint32_t(padding.toInt())
        )
    }
}
