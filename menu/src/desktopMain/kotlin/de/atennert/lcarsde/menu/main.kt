package de.atennert.lcarsde.menu

import com.sun.jna.Memory
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import de.atennert.lcarsde.menu.gtk.*
import java.util.concurrent.Executors


fun main() {
    val threadPool = Executors.newFixedThreadPool(1)

    GTK.INSTANCE.gtk_init(IntByReference(0), null)

    val windowListQ = MQ("/lcarswm-active-window-list", MQ.Mode.READ, false)
    val sendQ = MQ("/lcarswm-app-menu-messages", MQ.Mode.WRITE, false)

    val window = GtkWindow()
    val ui = GtkUI(window, sendQ)
    window.showAll()

    threadPool.execute {
        try {
            while (true) {
                windowListQ.receive()?.let {
                    val ptrRef = PointerByReference()
                    ptrRef.pointer = Memory(it.length.toLong() + 1)
                    ptrRef.pointer.setString(0, it)
                    GLib.INSTANCE.g_idle_add(
                        { dataPointer ->
                            val message = dataPointer?.getString(0)
                            if (message != null) {
                                updateWindowList(ui, message)
                            }
                            false
                        },
                        ptrRef
                    )
                }
                Thread.sleep(100)
            }
        } catch (e: InterruptedException) {
            println("stopped listening")
        }
    }

    window.connect("destroy") {
        threadPool.shutdownNow()
        GTK.INSTANCE.gtk_main_quit()
    }
    GTK.INSTANCE.gtk_main()

    windowListQ.close()
    sendQ.close()
}

private fun updateWindowList(ui: GtkUI, message: String) {
    val (type, windowData) = message.lines().run {
        Pair(this[0], this.drop(1))
    }
    if (type != "list") {
        return
    }
    ui.windowEntries = windowData.map { wd ->
        val (windowId, className, isActive) = wd.split("\t")
        WindowEntry(windowId, className, isActive == "active")
    }
}

private val CSS_PROVIDER = GTK.INSTANCE.gtk_css_provider_new()

class GtkUI(private val window: GtkWindow, private val sendQ: MQ) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(GtkOrientation.VERTICAL, 8)

    var windowEntries: List<WindowEntry> = emptyList()
        set(value) {
            updateWindows(value)
            field = value
            window.showAll()
        }

    private val gtkWindows = mutableMapOf<String, GtkWindowEntry>()

    init {
        GTK.INSTANCE.gtk_css_provider_load_from_path(CSS_PROVIDER, STYLE_PATH, null)
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(GtkPolicyType.NEVER, GtkPolicyType.AUTOMATIC)

        val spacer = GtkLabel("")
        spacer.setStyling(CSS_PROVIDER, "spacer")
        appContainer.packEnd(spacer, true, true, 0u)

        scrollContainer.add(appContainer)
        window.add(scrollContainer)

        window.connect("realize") { window.setUtf8Property(LCARSDE_APP_MENU, LCARSDE_APP_MENU) }
        window.connect("destroy") {}
    }

    private fun updateWindows(newWindowEntries: List<WindowEntry>) {
        val windowsToRemove = windowEntries.filter { !newWindowEntries.contains(it) }
        val windowsToAdd = newWindowEntries.filter { !windowEntries.contains(it) }

        windowsToRemove.forEach(::removeWindow)
        windowsToAdd.forEach(::addWindow)

        handleActivity(newWindowEntries)
    }

    private fun removeWindow(entry: WindowEntry) {
        gtkWindows[entry.id]?.let { appContainer.remove(it) }
    }

    private fun addWindow(entry: WindowEntry) {
        val gtkWindow = GtkWindowEntry(
            entry.id,
            entry.name,
            entry.isActive,
            ::selectWindow,
            ::closeWindow,
        )
        appContainer.packStart(gtkWindow, false, false, 0u)
        gtkWindows[entry.id] = gtkWindow
    }

    private fun handleActivity(entries: List<WindowEntry>) {
        entries.forEach { (id, _, isActive) ->
            gtkWindows[id]?.run {
                if (this.isActive != isActive) {
                    this.setActivity(isActive)
                }
            }
        }
    }

    private fun selectWindow(id: String) {
        sendQ.send("select\n$id")
    }

    private fun closeWindow(id: String) {
        sendQ.send("close\n$id")
    }

    companion object {
        const val STYLE_PATH = "/usr/share/lcarsde/menu/style.css"
        const val LCARSDE_APP_MENU = "LCARSDE_APP_MENU"
        const val CELL_SIZE = 40
        const val GAP_SIZE = 8
    }
}

class GtkWindowEntry(
    private val id: String,
    name: String,
    var isActive: Boolean,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit
) : GtkBox(GtkOrientation.HORIZONTAL, 8) {
    private val shortenedName = if (name.take(15) == name) name else "${name.take(15)}…"

    private val selectButton = GtkButton(shortenedName)
    private val closeButton = GtkButton()

    init {
        selectButton.setSize(184, 40)
        selectButton.setAlignment(1f, 1f)
        var selectButtonClasses = arrayOf("select_button")
        if (isActive) {
            selectButtonClasses += "selected"
        }
        selectButton.setStyling(CSS_PROVIDER, *selectButtonClasses)
        selectButton.onClick { onSelect(id) }
        packStart(selectButton, false, false, 0u)

        closeButton.setSize(32, 40)
        closeButton.setStyling(CSS_PROVIDER, "close_button")
        closeButton.onClick { onClose(id) }
        packStart(closeButton, false, false, 0u)
    }

    fun setActivity(isActive: Boolean) {
        this.isActive = isActive
        if (isActive) {
            this.addClass("selected")
        } else {
            this.removeClass("selected")
        }
    }
}
