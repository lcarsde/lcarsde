import de.atennert.gtk.GtkApplication
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarsde.log.PrintLogger
import de.atennert.lcarsde.statusbar.StatusBar
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import gtk.GtkWidget
import gtk.gtk_widget_show_all
import kotlinx.cinterop.*

@ExperimentalForeignApi
fun main() = gtkApplication {
    ServiceLocator.provide<Logger> { PrintLogger() }
    val statusBar = StatusBar()
    val ref = StableRef.create(this)

    gSignalConnect(
        statusBar.window,
        "destroy",
        staticCFunction { _: CPointer<GtkWidget>, r: COpaquePointer ->
            r.asStableRef<GtkApplication>().get().mainQuit()
        },
        ref.asCPointer()
    )
    gtk_widget_show_all(statusBar.window)

    main()
    StatusBar.stop()
}
