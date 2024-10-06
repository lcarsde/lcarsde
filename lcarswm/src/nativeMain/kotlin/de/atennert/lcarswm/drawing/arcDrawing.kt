package de.atennert.lcarswm.drawing

import kotlin.math.sqrt

fun getFilledArcOpacities(radius: Int): List<Triple<Int, Int, Double>> {
    val controlDistance = radius - .545 // determined by trying what feels ok
    val minDistanceFromCircle = 0
    val maxDistanceFromCircle = 1.1 // determined by trying what feels ok
    val pixelCenterOffset = .5 // the center of the circle is right between two pixels, not on a pixel

    val opacities = mutableListOf<Triple<Int, Int, Double>>()
    for (x in 0 until radius) {
        for (y in 0 until radius) {
            val x1 = x + pixelCenterOffset
            val y1 = y + pixelCenterOffset
            val distanceToCenter = sqrt(x1 * x1 + y1 * y1)
            val distanceToCircleLine = distanceToCenter - controlDistance
            when {
                distanceToCircleLine > minDistanceFromCircle && distanceToCircleLine < maxDistanceFromCircle ->
                    opacities.add(Triple(x, y, 1 - distanceToCircleLine / maxDistanceFromCircle))
                distanceToCircleLine <= minDistanceFromCircle -> opacities.add(Triple(x, y, 1.0))
            }
        }
    }
    return opacities
}
