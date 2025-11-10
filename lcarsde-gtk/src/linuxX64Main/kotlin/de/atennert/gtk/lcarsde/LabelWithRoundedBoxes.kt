package de.atennert.gtk.lcarsde

import de.atennert.gtk.CssProvider
import de.atennert.gtk.GtkBox
import de.atennert.gtk.GtkLabel
import gtk.GtkOrientation
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class LabelWithRoundedBoxes(text: String, cssProvider: CssProvider) :
    GtkBox(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 8) {
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