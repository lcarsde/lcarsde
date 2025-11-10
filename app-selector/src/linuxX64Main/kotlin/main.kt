import de.atennert.gtk.GtkWindow
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.appSelector.AppSelector
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
fun main() = gtkApplication {
    val window = GtkWindow()
    window.setTitle("App Selector")
    AppSelector(window)
    window.showAll()

    window.connect("destroy", this) { _, app -> app.mainQuit() }

    main()
}