package de.atennert.gtk

import com.sun.jna.ptr.IntByReference

actual fun gtkInit() = GTK.INSTANCE.gtk_init(IntByReference(0), null)

actual fun gtkMainIteration() = GTK.INSTANCE.gtk_main_iteration()

actual fun gtkEventsPending() = GTK.INSTANCE.gtk_events_pending()
