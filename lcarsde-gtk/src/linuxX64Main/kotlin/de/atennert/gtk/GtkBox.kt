package de.atennert.gtk

import gtk.gtk_box_new
import gtk.gtk_box_pack_end
import gtk.gtk_box_pack_start
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret

@OptIn(ExperimentalForeignApi::class)
open class GtkBox(
    orientation: gtk.GtkOrientation, spacing: Int,
    private val box: CPointer<gtk.GtkWidget>? = gtk_box_new(orientation, spacing)
) : GtkContainer(box) {

    fun packStart(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        gtk_box_pack_start(
            box?.reinterpret(),
            child.widget,
            expand.toInt(),
            fill.toInt(),
            padding
        )
    }

    fun packEnd(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        gtk_box_pack_end(
            box?.reinterpret(),
            child.widget,
            expand.toInt(),
            fill.toInt(),
            padding
        )
    }
}
