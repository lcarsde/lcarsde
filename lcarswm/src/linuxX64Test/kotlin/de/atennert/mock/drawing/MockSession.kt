package de.atennert.mock.drawing

import de.atennert.lcarswm.drawing.Color
import de.atennert.lcarswm.drawing.Image
import de.atennert.lcarswm.drawing.Session
import de.atennert.lcarswm.drawing.TextAlignment

object MockSession : Session {
    override fun drawImage(image: Image, x: Int, y: Int) {}

    override fun drawText(
        text: String,
        font: String?,
        x: Int,
        y: Int,
        height: Int,
        alignment: TextAlignment,
        textColor: Color,
        bgColor: Color?
    ) {
    }

    override fun drawBarEndLeft(x: Double, y: Double, color: Color) {}

    override fun drawBarEndRight(x: Double, y: Double, color: Color) {}

    override fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Color) {}

    override fun drawCornerUp(
        x: Int,
        y: Int,
        width: Int,
        sideWidth: Int,
        height: Int,
        barHeight: Int,
        rOuter: Int,
        rInner: Int,
        color: Color
    ) {
    }

    override fun drawCornerDown(
        x: Int,
        y: Int,
        width: Int,
        sideWidth: Int,
        height: Int,
        barHeight: Int,
        rOuter: Int,
        rInner: Int,
        color: Color
    ) {
    }

    override fun dispose() {}
}