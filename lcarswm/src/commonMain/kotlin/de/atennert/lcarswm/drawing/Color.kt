package de.atennert.lcarswm.drawing

private fun hexToRgb(hexColor: String): Triple<Int, Int, Int> {
    val colorRegex = Regex("^#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$")
    val (r, g, b) = colorRegex.matchEntire(hexColor)?.destructured
        ?: throw IllegalArgumentException("$hexColor is not an interpretable hexadecimal color")
    return Triple(r.toInt(16), g.toInt(16), b.toInt(16))
}

data class Color(val redSpec: Int, val greenSpec: Int, val blueSpec: Int, val opacity: Double = 1.0) {

    constructor(hexColor: String, opacity: Double = 1.0) : this(
        hexToRgb(hexColor).first,
        hexToRgb(hexColor).second,
        hexToRgb(hexColor).third,
        opacity
    )

    val red = (redSpec * 257 * opacity).toInt()
    val green = (greenSpec * 257 * opacity).toInt()
    val blue = (blueSpec * 257 * opacity).toInt()
}