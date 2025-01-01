package de.atennert.gtk

import gtk.gtk_main_iteration
import gtk.gtk_events_pending
import gtk.gtk_init
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cValuesOf

@ExperimentalForeignApi
actual fun gtkInit() = gtk_init(cValuesOf(0), cValue())

@ExperimentalForeignApi
actual fun gtkMainIteration() = gtk_main_iteration()

@ExperimentalForeignApi
actual fun gtkEventsPending() = gtk_events_pending()
