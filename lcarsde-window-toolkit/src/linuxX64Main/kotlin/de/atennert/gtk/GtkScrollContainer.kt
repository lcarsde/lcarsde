package de.atennert.gtk

import gtk.GtkWidget
import gtk.gtk_scrolled_window_new
import gtk.gtk_scrolled_window_set_policy
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret

@OptIn(ExperimentalForeignApi::class)
class GtkScrollContainer(private val scrollWindow: CPointer<GtkWidget>? = gtk_scrolled_window_new(null, null)) :
    GtkContainer(scrollWindow) {
    fun setPolicy(hScrollbarPolicy: gtk.GtkPolicyType, vScrollbarPolicy: gtk.GtkPolicyType) {
        gtk_scrolled_window_set_policy(scrollWindow?.reinterpret(), hScrollbarPolicy, vScrollbarPolicy)
    }
}
