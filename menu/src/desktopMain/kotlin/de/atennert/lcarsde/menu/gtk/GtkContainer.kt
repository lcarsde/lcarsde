package de.atennert.lcarsde.menu.gtk

import com.sun.jna.Pointer

open class GtkContainer(container: Pointer) : GtkWidget(container) {
    fun add(child: GtkWidget) {
        GTK.INSTANCE.gtk_container_add(this.widget, child.widget)
    }

    fun remove(child: GtkWidget) {
        GTK.INSTANCE.gtk_container_remove(this.widget, child.widget)
    }
}