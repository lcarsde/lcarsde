package de.atennert.gtk

import gtk.gtk_flow_box_new
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class GtkFlowBox : GtkContainer(gtk_flow_box_new())