package de.atennert.gtk

import gtk._GtkWidget
import gtk.gtk_button_new
import gtk.gtk_button_set_alignment
import gtk.gtk_button_set_label
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret

@OptIn(ExperimentalForeignApi::class)
class GtkButton(label: String? = null, private val button: CPointer<gtk.GtkWidget>? = gtk_button_new()) :
    GtkWidget(button) {
    init {
        if (label != null) {
            gtk_button_set_label(button?.reinterpret(), label)
        }
    }

    fun setAlignment(xalign: Float, yalign: Float) {
        gtk_button_set_alignment(button?.reinterpret(), xalign, yalign)
    }

    inline fun <reified T : Any> onClick(
        data: T,
        noinline callback: (CPointer<_GtkWidget>, T) -> Unit
    ) {
        this.connect("clicked", data, callback)
    }
}