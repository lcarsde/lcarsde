package de.atennert.lcarswm.drawing

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ColorTest {
    @Test
    fun `create color from ints`() {
        val color = Color(0x11, 0x22, 0x33)

        color.red shouldBe 0x1111
        color.green shouldBe 0x2222
        color.blue shouldBe 0x3333
    }

    @Test
    fun `create color from string`() {
        val color = Color("#112233")

        color.red shouldBe 0x1111
        color.green shouldBe 0x2222
        color.blue shouldBe 0x3333
    }
}