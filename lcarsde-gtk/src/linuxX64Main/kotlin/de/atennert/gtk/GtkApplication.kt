package de.atennert.gtk

import gtk.gtk_events_pending
import gtk.gtk_init
import gtk.gtk_main_iteration
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cValuesOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalForeignApi::class)
class GtkApplication(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private var stop = false

    init {
        gtk_init(cValuesOf(0), cValue())
    }

    fun main() = runBlocking {
        while (!stop) {
            while (gtk_events_pending() != 0) {
                gtk_main_iteration()
            }
            delay(50)
        }
    }

    fun mainQuit() {
        stop = true
    }
}


fun gtkApplication(f: suspend GtkApplication.() -> Unit) = runBlocking {
    f(GtkApplication(coroutineContext))
}