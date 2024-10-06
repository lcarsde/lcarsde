package de.atennert.lcarswm.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ClosableTest {
    @Test
    fun `closables are closed`() {
        val closingMock = ClosingMock().closeWith(ClosingMock::close)

        closeClosables()

        assertEquals(1, closingMock.closes)
    }

    @Test
    fun `tolerate failing closables`() {
        val closingFunction = { closing: ClosingMock ->
            closing.close()
            throw RuntimeException()
        }
        val closingMock1 = ClosingMock().closeWith(closingFunction)
        val closingMock2 = ClosingMock().closeWith(closingFunction)

        closeClosables()

        assertEquals(1, closingMock1.closes)
        assertEquals(1, closingMock2.closes)
    }

    class ClosingMock {
        var closes = 0

        fun close() {
            closes++
        }
    }
}