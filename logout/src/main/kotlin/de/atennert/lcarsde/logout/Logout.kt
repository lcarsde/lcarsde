package de.atennert.lcarsde.logout

import de.atennert.gtk.*
import de.atennert.lcarsde.logout.definition.DBusDefinition
import de.atennert.lcarsde.logout.definition.LockScreenDefinition
import de.atennert.lcarsde.logout.definition.LogoutDefinition

private val definitions = arrayOf(
    DBusDefinition("Shutdown", LcarsColors.C_C66, "Stop", "PowerOff"),
    DBusDefinition("Reboot", LcarsColors.C_F96, "Restart", "Reboot"),
    DBusDefinition("Suspend", LcarsColors.C_C9C, null, "Suspend"),
    DBusDefinition("Hibernate", LcarsColors.C_C9C, null, "Hibernate"),
    LockScreenDefinition(),
    LogoutDefinition(),
).filter { it.isAvailable }

private val CSS_PROVIDER = GTK.INSTANCE.gtk_css_provider_new()
private val STYLE_PATH = "/usr/share/lcarsde/logout/style.css"

class Logout(window: GtkWindow) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(GtkOrientation.VERTICAL, 8)

    init {
        GTK.INSTANCE.gtk_css_provider_load_from_path(CSS_PROVIDER, STYLE_PATH, null)
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(GtkPolicyType.NEVER, GtkPolicyType.AUTOMATIC)

        appContainer.setAlign(GtkAlignment.CENTER, GtkAlignment.CENTER)

        definitions.forEach {
            val button = GtkButton(it.label)
            button.setAlignment(1f, 1f)
            button.setStyling(CSS_PROVIDER, "button", "button-${it.color.color}")
            button.onClick(it::call)
            appContainer.packStart(button, false, false, 0u)
        }

        scrollContainer.add(appContainer)
        window.add(scrollContainer)
    }
}

fun main() = gtkApplication {
    val window = GtkWindow()
    window.setTitle("Logout")
    Logout(window)
    window.showAll()

    window.connect("destroy", ::mainQuit)
    main()
}