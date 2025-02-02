package de.atennert.lcarsde.statusbar.configuration

/**
 * Widget entry of the status bar configuration.
 */
data class WidgetConfiguration(
    val name: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    var addedPx = 0
        private set

    var properties = emptyMap<String, String>()
        private set

    fun withProperties(properties: Map<String, String>): WidgetConfiguration {
        this.properties = properties
        return this
    }

    fun withAddedPx(addedPx: Int): WidgetConfiguration {
        this.addedPx = addedPx
        return this
    }
}
