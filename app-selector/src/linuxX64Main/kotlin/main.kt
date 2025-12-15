import de.atennert.gtk.GtkWindow
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.appSelector.AppSelector
import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarsde.log.PrintLogger
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
fun main() = gtkApplication {
    ServiceLocator.provide<Logger> { PrintLogger() }
    val window = GtkWindow()
    window.setTitle("App Selector")
    AppSelector(window)
    window.showAll()

    window.connect("destroy", this) { _, app -> app.mainQuit() }

    main()
}