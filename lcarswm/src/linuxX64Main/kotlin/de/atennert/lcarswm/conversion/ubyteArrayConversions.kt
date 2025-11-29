package de.atennert.lcarswm.conversion

import kotlinx.cinterop.*

/** Convert ULong value to byte array as used in X properties */
fun ULong.toUByteArray(): UByteArray {
    return ubyteArrayOf(
        (this and 0xFF.toULong()).toUByte(),
        (this.shr(8) and 0xFF.toULong()).toUByte(),
        (this.shr(16) and 0xFF.toULong()).toUByte(),
        (this.shr(24) and 0xFF.toULong()).toUByte()
    )
}

/** Convert ULongArray to byte array as used in X properties */
fun ULongArray.toUByteArray(): UByteArray {
    return this
        .flatMap { it.toUByteArray() }
        .toUByteArray()
}

/** Convert a UByteArray to a ULongArray */
fun UByteArray.toULongArray(): ULongArray {
    if (this.size % 4 != 0) {
        throw ArithmeticException("UByte array needs to be divisible by 4 for conversion to ULong")
    }
    return ULongArray(this.size / 4) {
        val pos = it * 4
        this[pos].toULong() +
                this[pos + 1].toULong().shl(8) +
                this[pos + 2].toULong().shl(16) +
                this[pos + 3].toULong().shl(24)
    }
}

/** Convert string to byte array as used in X properties */
fun String.toUByteArray(): UByteArray {
    return this.encodeToByteArray().asUByteArray()
}

/** Concatenate UByte arrays to a single UByte array */
fun List<UByteArray>.combine(): UByteArray {
    val byteList = this.fold(mutableListOf<UByte>()) { list, bytes -> list.addAll(bytes); list }
    return byteList.toUByteArray()
}

/** convert this ubyte array pointer to a string */
@ExperimentalForeignApi
fun CPointer<UByteVar>.toKString(): String {
    val byteString = mutableListOf<Byte>()
    var i = 0

    while (true) {
        val value = this[i]
        if (value.toInt() == 0) {
            break
        }

        byteString.add(value.toByte())
        i++
    }
    return byteString.toByteArray().toKString()
}

/** convert this ubyte array pointer to a string */
@ExperimentalForeignApi
fun CPointer<UByteVar>?.toKString(): String = this?.toKString() ?: ""
