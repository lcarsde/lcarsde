package de.atennert

import com.sun.jna.IntegerType

class Uint32_t @JvmOverloads constructor(private val value: Int = 0) :
    IntegerType(4, value.toLong(), true) {
    override fun toByte() = value.toByte()

    override fun toShort() = value.toShort()
}
