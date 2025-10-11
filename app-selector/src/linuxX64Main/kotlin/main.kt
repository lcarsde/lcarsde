import de.atennert.gtk.GtkApplication
import de.atennert.gtk.GtkWindow
import de.atennert.gtk.NativeCallbackRef
import de.atennert.gtk.NativeSignalDataRef
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.appSelector.AppSelector
import gtk.GtkWidget
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction

@ExperimentalForeignApi
fun main() = gtkApplication {
    val window = GtkWindow()
    window.setTitle("Logout")
    AppSelector(window)
    window.showAll()

    window.connect("destroy",
        NativeCallbackRef((staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer ->
            p.asStableRef<GtkApplication>().get().mainQuit()
        }).reinterpret()),
        NativeSignalDataRef(StableRef.create(this).asCPointer())
    )
    main()
}