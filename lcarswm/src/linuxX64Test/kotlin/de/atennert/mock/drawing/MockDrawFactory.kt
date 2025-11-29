package de.atennert.mock.drawing

import de.atennert.lcarswm.drawing.DrawFactory
import de.atennert.lcarswm.drawing.Surface

object MockDrawFactory : DrawFactory {
    override fun createSurface(configuration: Surface.Configuration) = MockSurface

    override fun createImageFromFile(path: String) = MockImage
}