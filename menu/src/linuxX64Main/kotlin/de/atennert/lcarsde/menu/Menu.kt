package de.atennert.lcarsde.menu

import de.atennert.gtk.*
import de.atennert.lcarsde.comm.MessageQueue
import de.atennert.lcarsde.menu.Menu.Companion.STYLE_PATH
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
private val CSS_PROVIDER = CssProvider.fromPath(STYLE_PATH)

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
class Menu(private val window: GtkWindow, private val sendQ: MessageQueue) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(gtk.GtkOrientation.GTK_ORIENTATION_VERTICAL, 8)

    private var windowEntries: List<WindowEntry> = emptyList()
        set(value) {
            updateWindows(value)
            field = value
        }

    private val gtkWindows = mutableMapOf<String, GtkWindowEntry>()

    init {
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(gtk.GtkPolicyType.GTK_POLICY_NEVER, gtk.GtkPolicyType.GTK_POLICY_AUTOMATIC)

        val spacer = GtkLabel("")
        spacer.setStyling(CSS_PROVIDER, "spacer")
        appContainer.packEnd(spacer, true, true, 0u)

        scrollContainer.add(appContainer)
        window.add(scrollContainer)

        window.connect("realize", window) { _, window ->
            window.setUtf8Property(LCARSDE_APP_MENU, LCARSDE_APP_MENU)
        }
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
        val windowsToReplace =
            windowEntries.minus { windowsToRemove }.mapNotNull { newWindowEntries.find { newIt -> it == newIt } }
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
            entry.name,
            entry.isActive,
            { selectWindow(entry.id) },
            { closeWindow(entry.id) },
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

@OptIn(ExperimentalForeignApi::class)
class GtkWindowEntry(
    name: String,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) : GtkBox(gtk.GtkOrientation.GTK_ORIENTATION_HORIZONTAL, Menu.GAP_SIZE) {
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
        selectButton.onClick(onSelect) { _, f -> f.invoke() }
        packStart(selectButton, false, false, 0u)

        closeButton.setSize(32, Menu.CELL_SIZE)
        closeButton.setStyling(CSS_PROVIDER, "close_button")
        closeButton.onClick(onClose) { _, f -> f.invoke() }
        packStart(closeButton, false, false, 0u)
    }
}
