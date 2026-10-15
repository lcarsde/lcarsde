package de.atennert.gtk

import gtk.gtk_label_new
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class GtkLabel(text: String) : GtkWidget(gtk_label_new(text))