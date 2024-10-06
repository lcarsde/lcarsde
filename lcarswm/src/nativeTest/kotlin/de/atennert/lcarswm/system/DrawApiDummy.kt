package de.atennert.lcarswm.system

import de.atennert.lcarswm.system.api.DrawApi
import kotlinx.cinterop.*
import xlib.*

@ExperimentalForeignApi
open class DrawApiDummy : DrawApi {
    override fun fillRectangle(
        drawable: Drawable,
        graphicsContext: GC,
        x: Int,
        y: Int,
        width: UInt,
        height: UInt
    ): Int {
        throw NotImplementedError()
    }

    override fun fillRectangles(
        drawable: Drawable,
        graphicsContext: GC,
        rects: CValuesRef<XRectangle>,
        rectCount: Int
    ): Int {
        throw NotImplementedError()
    }

    override fun drawPoint(drawable: Drawable, graphicsContext: GC, x: Int, y: Int): Int {
        throw NotImplementedError()
    }

    override fun putImage(
        drawable: Drawable,
        graphicsContext: GC,
        image: CValuesRef<XImage>,
        x: Int,
        y: Int,
        width: UInt,
        height: UInt
    ): Int {
        throw NotImplementedError()
    }

    override fun createGC(drawable: Drawable, mask: ULong, gcValues: CValuesRef<XGCValues>?): GC? {
        throw NotImplementedError()
    }

    override fun freeGC(graphicsContext: GC): Int {
        throw NotImplementedError()
    }

    override fun createColormap(window: Window, visual: CValuesRef<Visual>, alloc: Int): Colormap {
        throw NotImplementedError()
    }

    override fun allocColor(colorMap: Colormap, color: CPointer<XColor>): Int {
        throw NotImplementedError()
    }

    override fun freeColors(colorMap: Colormap, pixels: CValuesRef<ULongVar>, pixelCount: Int): Int {
        throw NotImplementedError()
    }

    override fun freeColormap(colorMap: Colormap): Int {
        throw NotImplementedError()
    }

    override fun readXpmFileToImage(imagePath: String, imageBuffer: CPointer<CPointerVar<XImage>>): Int {
        throw NotImplementedError()
    }

    override fun createPixmap(drawable: Drawable, width: UInt, height: UInt, depth: UInt): Pixmap {
        throw NotImplementedError()
    }

    override fun setWindowBackgroundPixmap(window: Window, pixmap: Pixmap) {
        throw NotImplementedError()
    }

    override fun clearWindow(window: Window) {
        throw NotImplementedError()
    }

    override fun freePixmap(pixmap: Pixmap) {
        throw NotImplementedError()
    }

    override fun xftDrawCreate(
        drawable: Drawable,
        visual: CValuesRef<Visual>,
        colorMap: Colormap
    ): CPointer<XftDraw>? {
        throw NotImplementedError()
    }

    override fun xftDrawRect(
        xftDraw: CPointer<XftDraw>?,
        color: CPointer<XftColor>,
        x: Int,
        y: Int,
        width: UInt,
        height: UInt
    ) {
        throw NotImplementedError()
    }
}