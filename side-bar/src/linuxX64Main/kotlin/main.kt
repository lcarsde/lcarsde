import de.atennert.gtk.GtkWindow
import de.atennert.gtk.gtkApplication
import de.atennert.lcarsde.comm.MessageQueue
import de.atennert.lcarsde.lifecycle.closeClosables
import de.atennert.lcarsde.sideBar.SideBar
import gtk.g_idle_add
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalNativeApi

@ExperimentalForeignApi
fun main() = gtkApplication {
    val windowListQ = MessageQueue("/lcarswm-active-window-list", MessageQueue.Mode.READ, false)
    val sendQ = MessageQueue("/lcarswm-app-menu-messages", MessageQueue.Mode.WRITE, false)

    val window = GtkWindow()
    window.setTitle("Side Bar")
    val sideBar = SideBar(window, sendQ)
    window.showAll()

    val job = readWindowUpdates(windowListQ, sideBar)

    window.connect("destroy", this) { _, app -> app.mainQuit() }
    main()

    job.cancelAndJoin()
    closeClosables()
}

class MessageEvent(private val message: String, private val sideBar: SideBar) {
    fun updateWindowList() = sideBar.updateWindowList(message)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
private fun CoroutineScope.readWindowUpdates(windowListQ: MessageQueue, sideBar: SideBar) = launch {
    while (true) {
        windowListQ.receive()?.let { message ->
            val messageEventRef = StableRef.create(MessageEvent(message, sideBar))
            g_idle_add(
                staticCFunction { dataPointer ->
                    val messageEvent = dataPointer?.asStableRef<MessageEvent>()?.get()
                    messageEvent?.updateWindowList()
                    0
                },
                messageEventRef.asCPointer(),
            )
        }
        delay(100)
    }
}
