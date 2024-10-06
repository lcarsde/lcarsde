package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.settings.GeneralSetting
import de.atennert.lcarswm.system.api.FontApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.nativeHeap
import xlib.*

@ExperimentalForeignApi
class FontProvider(
    private val fontApi: FontApi,
    private val generalSettings: Map<GeneralSetting, String>,
    screenId: Int
) {
    val pango = fontApi.xftGetContext(screenId)
    val layout = fontApi.newLayout(pango)
    val font = fontApi.getFontDescription()

    init {
        initializeFontObjects()

        closeWith(FontProvider::close)
    }

    private fun initializeFontObjects() {
        fontApi.setFontDescriptionFamily(font, generalSettings.getValue(GeneralSetting.FONT))
        fontApi.setFontDescriptionStyle(font, PangoStyle.PANGO_STYLE_NORMAL)

        fontApi.setLayoutWrapMode(layout, PangoWrapMode.PANGO_WRAP_WORD_CHAR)
        fontApi.setLayoutEllipsizeMode(layout, PangoEllipsizeMode.PANGO_ELLIPSIZE_END)
        fontApi.setLayoutSingleParagraphMode(layout, true)
    }

    fun getAscDsc(fontSize: Int, fontWeight: PangoWeight): Pair<Int, Int> {
        fontApi.setFontDescriptionWeight(font, fontWeight)
        fontApi.setFontDescriptionSize(font, fontSize * PANGO_SCALE)
        fontApi.setLayoutFontDescription(layout, font)

        val lang = fontApi.getDefaultLanguage()
        val metrics = fontApi.getFontMetrics(pango, font, lang)
        val ascDesc = fontApi.getFontAscentDescent(metrics)

        fontApi.freeFontMetrics(metrics)

        return ascDesc
    }

    private fun close() {
        layout?.let { nativeHeap.free(it.rawValue) }
        fontApi.freeFontDescription(font)
        pango?.let { nativeHeap.free(it.rawValue) }
    }
}