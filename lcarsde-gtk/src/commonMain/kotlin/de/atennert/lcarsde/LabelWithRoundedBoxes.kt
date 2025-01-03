package de.atennert.lcarsde

import de.atennert.gtk.*

class LabelWithRoundedBoxes(text: String, cssProvider: CssProviderRef) : GtkBox(GtkOrientation.HORIZONTAL, 8) {
    init {
        val lineEndLeft = GtkLabel("")
        lineEndLeft.setStyling(cssProvider, "line-end", "line-end--left")
        add(lineEndLeft)

        val categoryLabel = GtkLabel(text)
        categoryLabel.setStyling(cssProvider, "category")
        add(categoryLabel)

        val lineEndRight = GtkLabel("")
        lineEndRight.setStyling(cssProvider, "line-end", "line-end--right")
        add(lineEndRight)
    }
}