package de.atennert.gtk

import gtk.gtk_container_add
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret

@OptIn(ExperimentalForeignApi::class)
open class GtkContainer(private val container: CPointer<gtk.GtkWidget>?) : GtkWidget(container) {
    fun add(child: GtkWidget) {
        gtk_container_add(this.container?.reinterpret(), child.widget)
    }

    fun remove(child: GtkWidget) {
        gtk_container_add(this.container?.reinterpret(), child.widget)
    }
}