package de.atennert.lcarsde.statusbar.widgets

import gtk.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Creates the font description for text in the Status Bar. Free it with pango_font_description_free.
 *
 * Older font packages have a separate "Ubuntu Condensed" family, newer ones only the variable
 * "Ubuntu" font with a condensed width, so ask for both and for the condensed stretch.
 */
@ExperimentalForeignApi
fun createFontDescription(size: Int): CPointer<PangoFontDescription>? {
    val fontDescription = pango_font_description_from_string("Ubuntu Condensed,Ubuntu $size")
    pango_font_description_set_stretch(fontDescription, PangoStretch.PANGO_STRETCH_CONDENSED)
    return fontDescription
}
