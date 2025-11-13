package de.atennert.lcarswm.drawing

import de.atennert.lcarsde.file.Files
import de.atennert.lcarswm.*

class ColorLoader(files: Files) {
//    private val configuredColors = files.readLines()

    var logoColor = YELLOW
        private set
    var activeTitleColor = YELLOW
        private set
    var inactiveTitleColor = DARK_RED
        private set
    var backgroundColor = BLACK
        private set

    var normalBarDownColor = BRIGHT_PURPLE
        private set
    var normalSidebarUpColor = DAMPENED_PURPLE
        private set
    var normalSidebarDownColor = DAMPENED_PURPLE
        private set
    var normalBarMiddle1Color = DAMPENED_PURPLE
        private set
    var normalBarMiddle2Color = DARK_RED
        private set
    var normalBarMiddle3Color = BRIGHT_PURPLE
        private set
    var normalBarMiddle4Color = ORCHID
        private set
    var normalCorner1Color = ORCHID
        private set
    var normalCorner2Color = ORCHID
        private set
    var normalCorner3Color = ORCHID
        private set

    var maxBarDownColor = ORCHID
        private set
}