package de.atennert.lcarswm.drawing

fun getArcs(
    baseColor: Color,
    opacities: List<Triple<Int, Int, Double>>,
    radius: Int,
    vararg quadrants: Int
): List<Triple<Int, Int, Color>> {
    val colors = mutableListOf<Triple<Int, Int, Color>>()
    val adjust = { opacity: Double ->
        baseColor.run { Color(redSpec, greenSpec, blueSpec, opacity) }
    }

    for ((x, y, opacity) in opacities) {
        for (quadrant in quadrants) {
            when (quadrant) {
                1 -> colors.add(Triple(x + radius, -y + radius - 1, adjust(opacity)))
                2 -> colors.add(Triple(-x + radius - 1, -y + radius - 1, adjust(opacity)))
                3 -> colors.add(Triple(-x + radius - 1, y + radius, adjust(opacity)))
                4 -> colors.add(Triple(x + radius, y + radius, adjust(opacity)))
                else -> throw IllegalArgumentException("There are only quadrants 1 to 4, not $quadrant")
            }
        }
    }
    return colors
}

