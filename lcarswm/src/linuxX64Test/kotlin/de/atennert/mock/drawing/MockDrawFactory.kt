package de.atennert.mock.drawing

import de.atennert.lcarswm.drivenPorts.drawing.DrawFactory
import de.atennert.lcarswm.drivenPorts.drawing.Surface

object MockDrawFactory : DrawFactory {
    override fun createSurface(configuration: Surface.Configuration) = MockSurface

    override fun createImageFromFile(path: String) = MockImage
}