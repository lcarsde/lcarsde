package de.atennert.mock.drawing

import de.atennert.lcarswm.drawing.Image

object MockImage : Image {
    override val width = 0
    override fun dispose() {}
}
