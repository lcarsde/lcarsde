import de.atennert.gtk.GtkWindow
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.logout.Logout
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
fun main() = gtkApplication {
    val window = GtkWindow()
    window.setTitle("Logout")
    Logout(window)
    window.showAll()

    window.connect("destroy", this) { _, app -> app.mainQuit() }
    main()
}