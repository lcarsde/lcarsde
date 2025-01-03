package de.atennert.lcarsde.appSelector

import de.atennert.gtk.*
import de.atennert.lcarsde.LabelWithRoundedBoxes

private val CSS_PROVIDER = gtkCssProviderNew()
private const val STYLE_PATH = "/usr/share/lcarsde/appSelector/style.css"

class AppSelector(window: GtkWindow) {
    private val scrollContainer = GtkScrollContainer()
    private val appContainer = GtkBox(GtkOrientation.VERTICAL, 8)

    init {
        val appManager = AppManager()

        gtkCssProviderLoadFromPath(CSS_PROVIDER, STYLE_PATH)
        window.setStyling(CSS_PROVIDER, "window")

        scrollContainer.setPolicy(GtkPolicyType.NEVER, GtkPolicyType.AUTOMATIC)

        appManager.appsByCategory.map { (category, apps) -> Pair(category, apps) }
            .sortedBy { it.first }
            .forEach { (category, apps) ->
                appContainer.add(LabelWithRoundedBoxes(category, CSS_PROVIDER))

                val flowBox = GtkFlowBox()
                apps.sortedBy { it.name }.forEach { app ->
                    val button = GtkButton(app.name)
                    button.setStyling(CSS_PROVIDER, "button", "button-${app.color.color}")
                    button.setAlignment(1f, 1f)
                    button.onClick(app::start)
                    flowBox.add(button)
                }
                appContainer.add(flowBox)
            }

        scrollContainer.add(appContainer)
        window.add(scrollContainer)
    }
}

fun main() = gtkApplication {
    val window = GtkWindow()
    window.setTitle("Logout")
    AppSelector(window)
    window.showAll()

    window.connect("destroy", ::mainQuit)
    main()
}