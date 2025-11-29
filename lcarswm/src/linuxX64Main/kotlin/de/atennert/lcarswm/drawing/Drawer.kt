package de.atennert.lcarswm.drawing

import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Display
import xlib.Drawable
import xlib.Visual

enum class TextAlignment {
    LEFT, RIGHT
}

interface DrawFactory {
    fun createSurface(configuration: Surface.Configuration): Surface

    fun createImageFromFile(path: String): Image
}

interface Session {
    fun drawImage(image: Image, x: Int, y: Int)

    fun drawText(
        text: String,
        font: String?,
        x: Int,
        y: Int,
        height: Int,
        alignment: TextAlignment,
        textColor: Color,
        bgColor: Color? = null
    )

    fun drawBarEndLeft(x: Double, y: Double, color: Color)

    fun drawBarEndLeft(x: Int, y: Int, color: Color) = drawBarEndLeft(x.toDouble(), y.toDouble(), color)

    fun drawBarEndRight(x: Double, y: Double, color: Color)

    fun drawBarEndRight(x: Int, y: Int, color: Color) = drawBarEndRight(x.toDouble(), y.toDouble(), color)

    fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Color)

    fun drawCornerUp(
        x: Int, y: Int,
        width: Int, sideWidth: Int,
        height: Int, barHeight: Int,
        rOuter: Int, rInner: Int,
        color: Color
    )

    fun drawCornerDown(
        x: Int, y: Int,
        width: Int, sideWidth: Int,
        height: Int, barHeight: Int,
        rOuter: Int, rInner: Int,
        color: Color
    )

    fun dispose()
}

interface Image {
    val width: Int

    fun dispose()
}

interface Surface {
    fun setSize(width: Int, height: Int)

    fun createSession(): Session

    fun dispose()

    @OptIn(ExperimentalForeignApi::class)
    data class Configuration(
        val display: CValuesRef<Display>?,
        val drawable: Drawable,
        val visual: CValuesRef<Visual>?,
        val width: Int,
        val height: Int
    )
}