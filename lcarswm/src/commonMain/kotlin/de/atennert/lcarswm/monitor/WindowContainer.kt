package de.atennert.lcarswm.monitor

import kotlin.math.min

data class WindowContainer(val x: Int, val y: Int, val width: Int, val height: Int) {

    fun shouldSplitHorizontally(minWidth: Int) = width / 2 >= minWidth

    fun splitHorizontally(): Pair<WindowContainer, WindowContainer> {
        val halfWidth = width / 2
        return Pair(
            WindowContainer(
                x, y, halfWidth, height
            ), WindowContainer(
                x + halfWidth, y, halfWidth, height
            )
        )
    }

    fun shouldSplitVertically(minHeight: Int) = height / 2 >= minHeight

    fun splitVertically(): Pair<WindowContainer, WindowContainer> {
        val halfHeight = height / 2
        return Pair(
            WindowContainer(
                x, y, width, halfHeight
            ), WindowContainer(
                x, y + halfHeight, width, halfHeight
            )
        )
    }

    fun canCombineWithHorizontally(other: WindowContainer) = y == other.y && height == other.height

    fun combineWithHorizontally(other: WindowContainer) = WindowContainer(
        min(x, other.x),
        y,
        width + other.width,
        height
    )

    fun canCombineWithVertically(other: WindowContainer) = x == other.x && width == width

    fun combineWithVertically(other: WindowContainer) = WindowContainer(
        x,
        min(y, other.y),
        width,
        height + other.height
    )
}