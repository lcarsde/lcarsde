package de.atennert.lcarsde.statusbar.extensions

import kotlinx.cinterop.*
import kotlinx.cinterop.toKString

/** Convert string to byte array as used in X properties */
fun String.toUByteArray(): UByteArray {
    return this.encodeToByteArray().asUByteArray()
}

/** convert this ubyte array pointer to a string */
@ExperimentalForeignApi
fun CPointer<UByteVar>.toKString(): String {
    val byteString = mutableListOf<Byte>()
    var i = 0

    while (true) {
        val value = this[i]
        if (value.convert<Int>() == 0) {
            break
        }

        byteString.add(value.convert())
        i++
    }
    return byteString.toByteArray().toKString()
}

/** convert this ubyte array pointer to a string */
@ExperimentalForeignApi
fun CPointer<UByteVar>?.toKString(): String = this?.toKString() ?: ""

/** print a float with a certain amount of places */
fun Float.print(places: Int): String {
    val flString = this.toString().split('.', limit = 2)
    return "${flString[0]}.${flString[1].take(places)}"
}
