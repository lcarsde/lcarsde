package de.atennert.lcarsde.appSelector

import de.atennert.gtk.*
import de.atennert.gtk.lcarsde.LabelWithRoundedBoxes
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
private const val STYLE_PATH = "/usr/share/lcarsde/appSelector/style.css"
private val CSS_PROVIDER = CssProvider.fromPath(STYLE_PATH)

@OptIn(ExperimentalForeignApi::class)
class AppSelector(window: GtkWindow) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(gtk.GtkOrientation.GTK_ORIENTATION_VERTICAL, 8)

    init {
        val appManager = AppManager()

        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(gtk.GtkPolicyType.GTK_POLICY_NEVER, gtk.GtkPolicyType.GTK_POLICY_AUTOMATIC)

        appManager.appsByCategory.map { (category, apps) -> Pair(category, apps) }
            .sortedBy { it.first }
            .forEach { (category, apps) ->
                appContainer.add(LabelWithRoundedBoxes(category, CSS_PROVIDER))

                val flowBox = GtkFlowBox()
                apps.sortedBy { it.name }.forEach { app ->
                    val button = GtkButton(app.name)
                    button.setStyling(CSS_PROVIDER, "button", "button-${app.color.color}")
                    button.setAlignment(1f, 1f)
                    button.onClick(app) { _, app ->
                        println("Run ${app.name}")
                        app.start()
                    }
                    flowBox.add(button)
                }
                appContainer.add(flowBox)
            }

        scrollContainer.add(appContainer)
        window.add(scrollContainer)
    }
}
