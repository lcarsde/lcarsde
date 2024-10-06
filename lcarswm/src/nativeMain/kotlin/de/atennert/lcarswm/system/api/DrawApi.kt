package de.atennert.lcarswm.system.api

import kotlinx.cinterop.*
import xlib.*

/**
 * API interface for X drawing functions
 */
@ExperimentalForeignApi
interface DrawApi {
    fun fillRectangle(drawable: Drawable, graphicsContext: GC, x: Int, y: Int, width: UInt, height: UInt): Int

    fun fillRectangles(drawable: Drawable, graphicsContext: GC, rects: CValuesRef<XRectangle>, rectCount: Int): Int

    fun drawPoint(drawable: Drawable, graphicsContext: GC, x: Int, y: Int): Int

    fun putImage(drawable: Drawable, graphicsContext: GC, image: CValuesRef<XImage>,
                  x: Int, y: Int, width: UInt, height: UInt): Int

    fun createGC(drawable: Drawable, mask: ULong, gcValues: CValuesRef<XGCValues>?): GC?

    fun freeGC(graphicsContext: GC): Int

    fun createColormap(window: Window, visual: CValuesRef<Visual>, alloc: Int): Colormap

    fun allocColor(colorMap: Colormap, color: CPointer<XColor>): Int

    fun freeColors(colorMap: Colormap, pixels: CValuesRef<ULongVar>, pixelCount: Int): Int

    fun freeColormap(colorMap: Colormap): Int

    fun readXpmFileToImage(imagePath: String, imageBuffer: CPointer<CPointerVar<XImage>>): Int

    fun createPixmap(drawable: Drawable, width: UInt, height: UInt, depth: UInt): Pixmap

    fun setWindowBackgroundPixmap(window: Window, pixmap: Pixmap)

    fun clearWindow(window: Window)

    fun freePixmap(pixmap: Pixmap)

    fun xftDrawCreate(drawable: Drawable, visual: CValuesRef<Visual>, colorMap: Colormap): CPointer<XftDraw>?

    fun xftDrawRect(xftDraw: CPointer<XftDraw>?, color: CPointer<XftColor>, x: Int, y: Int, width: UInt, height: UInt)
}