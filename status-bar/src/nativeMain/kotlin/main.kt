import de.atennert.lcarsde.statusbar.StatusBar
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import statusbar.gtk_events_pending
import statusbar.gtk_init
import statusbar.gtk_main_iteration
import statusbar.gtk_widget_show_all

var stop = false

@ExperimentalForeignApi
fun main() = runBlocking {
    gtk_init(cValuesOf(0), cValue())

    val statusBar = StatusBar()

    gSignalConnect(statusBar.window, "destroy", staticCFunction(::destroy))
    gtk_widget_show_all(statusBar.window)

    while(!stop) {
        while (gtk_events_pending() != 0) {
            gtk_main_iteration()
        }
        delay(50)
    }

    StatusBar.stop()
}

fun destroy() {
    stop = true
}
