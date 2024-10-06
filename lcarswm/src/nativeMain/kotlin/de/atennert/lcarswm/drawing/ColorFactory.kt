package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.system.api.DrawApi
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
class ColorFactory(private val drawApi: DrawApi, screen: Screen) {
    val colorMapId = drawApi.createColormap(screen.root, screen.root_visual!!, AllocNone)

    private val knownColors = mutableMapOf<Color, ULong>()
    private val knownGCs = mutableMapOf<Pair<Color, Drawable>, GC>()
    private val knownXftColors = mutableMapOf<Color, XftColor>()

    init {
        closeWith(ColorFactory::cleanupColorStuff)
    }

    private fun cleanupColorStuff() {
        val pixels = knownColors.values.toULongArray().toCValues()
        drawApi.freeColors(colorMapId, pixels, pixels.size)
        knownColors.clear()

        knownGCs.values.forEach { drawApi.freeGC(it) }
        knownGCs.clear()

        knownXftColors.values.forEach(nativeHeap::free)
        knownXftColors.clear()

        drawApi.freeColormap(colorMapId)
    }

    fun createColorGC(drawable: Drawable, color: Color): GC {
        val pixel = knownColors[color]
            ?: let {
                val xColor = color.toXColor()
                drawApi.allocColor(colorMapId, xColor.ptr)
                val px = xColor.pixel
                nativeHeap.free(xColor)
                knownColors[color] = px
                px
            }

        return knownGCs[Pair(color, drawable)]
            ?: let {
                val gcValues = createGcValues(pixel)
                val mask = GCForeground or GCGraphicsExposures or GCArcMode
                val gc = drawApi.createGC(drawable, mask.convert(), gcValues.ptr)!!
                nativeHeap.free(gcValues)
                knownGCs[Pair(color, drawable)] = gc
                gc
            }
    }

    private fun createGcValues(pixel: ULong): XGCValues {
        val gcValues = nativeHeap.alloc<XGCValues>()
        gcValues.foreground = pixel
        gcValues.graphics_exposures = 0
        gcValues.arc_mode = ArcPieSlice
        return gcValues
    }

    fun createXftColor(color: Color): XftColor {
        val pixel = knownColors[color]
            ?: let {
                val xColor = color.toXColor()
                drawApi.allocColor(colorMapId, xColor.ptr)
                val px = xColor.pixel
                nativeHeap.free(xColor)
                knownColors[color] = px
                px
            }

        return knownXftColors[color]
            ?: let {
                val xftColor = createXftColor(color, pixel)
                knownXftColors[color] = xftColor
                xftColor
            }
    }

    private fun createXftColor(color: Color, pixel: ULong): XftColor {
        val xftColor = nativeHeap.alloc<XftColor>()
        xftColor.color.red = color.red.convert()
        xftColor.color.green = color.green.convert()
        xftColor.color.blue = color.blue.convert()
        xftColor.color.alpha = 0xffff.convert()
        xftColor.pixel = pixel
        return xftColor
    }
}