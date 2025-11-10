package de.atennert.lcarsde.logout

import de.atennert.gtk.*
import de.atennert.lcarsde.logout.definition.DBusDefinition
import de.atennert.lcarsde.logout.definition.LockScreenDefinition
import de.atennert.lcarsde.logout.definition.LogoutDefinition
import de.atennert.lcarsde.logout.definition.LogoutOptionDefinition
import gtk.GtkAlign
import kotlinx.cinterop.ExperimentalForeignApi

private val definitions = arrayOf(
    DBusDefinition("Shutdown", LcarsColors.C_C66, "Stop", "PowerOff"),
    DBusDefinition("Reboot", LcarsColors.C_F96, "Restart", "Reboot"),
    DBusDefinition("Suspend", LcarsColors.C_C9C, null, "Suspend"),
    DBusDefinition("Hibernate", LcarsColors.C_C9C, null, "Hibernate"),
    LockScreenDefinition(),
    LogoutDefinition(),
).filter { it.isAvailable }

fun callDefinition(def: LogoutOptionDefinition) {
    def.call()
}

private const val STYLE_PATH = "/usr/share/lcarsde/logout/style.css"
private val CSS_PROVIDER = CssProvider.fromPath(STYLE_PATH)

@OptIn(ExperimentalForeignApi::class)
class Logout(window: GtkWindow) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(gtk.GtkOrientation.GTK_ORIENTATION_VERTICAL, 8)

    init {
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(gtk.GtkPolicyType.GTK_POLICY_NEVER, gtk.GtkPolicyType.GTK_POLICY_AUTOMATIC)

        appContainer.setAlign(GtkAlign.GTK_ALIGN_CENTER, GtkAlign.GTK_ALIGN_CENTER)

        definitions.forEach {
            val button = GtkButton(it.label)
            button.setAlignment(1f, 1f)
            button.setStyling(CSS_PROVIDER, "button", "button-${it.color.color}")
            button.onClick(it) { _, definition -> callDefinition(definition) }
            appContainer.packStart(button, false, false, 0u)
        }

        scrollContainer.add(appContainer)
        window.add(scrollContainer)
    }
}
