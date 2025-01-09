package de.atennert.lcarsde.menu

import com.sun.jna.Memory
import de.atennert.gtk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = gtkApplication {
    val windowListQ = MQ("/lcarswm-active-window-list", MQ.Mode.READ, false)
    val sendQ = MQ("/lcarswm-app-menu-messages", MQ.Mode.WRITE, false)

    val window = GtkWindow()
    val menu = Menu(window, sendQ)
    window.showAll()

    val job = readWindowUpdates(windowListQ, menu)

    window.connect("destroy", CallbackContainer(::mainQuit))
    main()

    job.cancelAndJoin()
    windowListQ.close()
    sendQ.close()
}

private fun CoroutineScope.readWindowUpdates(
    windowListQ: MQ,
    menu: Menu
) = launch {
    try {
        while (true) {
            windowListQ.receive()?.let {
                val data = Memory(it.length.toLong() + 1)
                data.setString(0, it)
                GLib.INSTANCE.g_idle_add(
                    { dataPointer ->
                        val message = dataPointer?.getString(0)
                        if (message != null) {
                            menu.updateWindowList(it)
                        }
                        false
                    },
                    data
                )
            }
            delay(100)
        }
    } catch (e: InterruptedException) {
        println("stopped listening")
    }
}

private val CSS_PROVIDER = gtkCssProviderNew()

class Menu(private val window: GtkWindow, private val sendQ: MQ) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(GtkOrientation.VERTICAL, 8)

    private var windowEntries: List<WindowEntry> = emptyList()
        set(value) {
            updateWindows(value)
            field = value
        }

    private val gtkWindows = mutableMapOf<String, GtkWindowEntry>()

    init {
        gtkCssProviderLoadFromPath(CSS_PROVIDER, STYLE_PATH)
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(GtkPolicyType.NEVER, GtkPolicyType.AUTOMATIC)

        val spacer = GtkLabel("")
        spacer.setStyling(CSS_PROVIDER, "spacer")
        appContainer.packEnd(spacer, true, true, 0u)

        scrollContainer.add(appContainer)
        window.add(scrollContainer)

        window.connect("realize", CallbackContainer { window.setUtf8Property(LCARSDE_APP_MENU, LCARSDE_APP_MENU) })
    }


    fun updateWindowList(message: String) {
        val (type, windowData) = message.lines().run {
            Pair(this[0], this.drop(1))
        }
        if (type != "list") {
            return
        }
        this.windowEntries = windowData.map { wd ->
            val (windowId, className, isActive) = wd.split("\t")
            WindowEntry(windowId, className, isActive == "active")
        }
        window.showAll()
    }

    private fun updateWindows(newWindowEntries: List<WindowEntry>) {
        val windowsToRemove = windowEntries.filter { !newWindowEntries.contains(it) }
        val windowsToReplace = windowEntries.minus { windowsToRemove }.map { newWindowEntries.first { newIt -> it == newIt } }
        val windowsToAdd = newWindowEntries.filter { !windowEntries.contains(it) }

        windowsToRemove.forEach(::removeWindow)
        windowsToReplace.forEach(::replaceWindow)
        windowsToAdd.forEach(::addWindow)
    }

    private fun removeWindow(entry: WindowEntry) {
        gtkWindows[entry.id]?.let { appContainer.remove(it) }
        gtkWindows.remove(entry.id)
    }

    /**
     * Evil hack ... we should rather adjust the styling of existing elements instead of creating new ones
     * by setting classes, but it's not working for some reason.
     */
    private fun replaceWindow(entry: WindowEntry) {
        gtkWindows[entry.id]?.let { appContainer.remove(it) }
        addWindow(entry)
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
        gtkWindow.showAll()
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
    id: String,
    name: String,
    isActive: Boolean,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit
) : GtkBox(GtkOrientation.HORIZONTAL, Menu.GAP_SIZE) {
    private val shortenedName = if (name.take(15) == name) name else "${name.take(15)}…"

    private val selectButton = GtkButton(shortenedName)
    private val closeButton = GtkButton()

    init {
        selectButton.setSize(184, Menu.CELL_SIZE)
        selectButton.setAlignment(1f, 1f)
        var selectButtonClasses = arrayOf("select_button")
        if (isActive) {
            selectButtonClasses += "selected"
        }
        selectButton.setStyling(CSS_PROVIDER, *selectButtonClasses)
        selectButton.onClick(CallbackContainer { onSelect(id) })
        packStart(selectButton, false, false, 0u)

        closeButton.setSize(32, Menu.CELL_SIZE)
        closeButton.setStyling(CSS_PROVIDER, "close_button")
        closeButton.onClick(CallbackContainer { onClose(id) })
        packStart(closeButton, false, false, 0u)
    }
}
