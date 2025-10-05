package de.atennert.lcarsde.logout

import de.atennert.gtk.*
import de.atennert.lcarsde.logout.definition.DBusDefinition
import de.atennert.lcarsde.logout.definition.LockScreenDefinition
import de.atennert.lcarsde.logout.definition.LogoutDefinition
import de.atennert.lcarsde.logout.definition.LogoutOptionDefinition
import gtk.GtkWidget
import kotlinx.cinterop.*

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

@OptIn(ExperimentalForeignApi::class)
private val CSS_PROVIDER = gtkCssProviderNew()
private const val STYLE_PATH = "/usr/share/lcarsde/logout/style.css"

@OptIn(ExperimentalForeignApi::class)
class Logout(window: GtkWindow) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(GtkOrientation.VERTICAL, 8)

    init {
        gtkCssProviderLoadFromPath(CSS_PROVIDER, STYLE_PATH)
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(GtkPolicyType.NEVER, GtkPolicyType.AUTOMATIC)

        appContainer.setAlign(GtkAlignment.CENTER, GtkAlignment.CENTER)

        definitions.forEach {
            val button = GtkButton(it.label)
            button.setAlignment(1f, 1f)
            button.setStyling(CSS_PROVIDER, "button", "button-${it.color.color}")
            button.onClick(
                NativeCallbackRef((staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer ->
                    callDefinition(p.asStableRef<LogoutOptionDefinition>().get())
                }).reinterpret()),
                NativeSignalDataRef(StableRef.create(it).asCPointer())
            )
            appContainer.packStart(button, false, false, 0u)
        }

        scrollContainer.add(appContainer)
        window.add(scrollContainer)
    }
}
