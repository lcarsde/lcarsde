package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class FilterTest {
    @Test
    fun `should filter values`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2, 3, 4)
            .apply(filter { it % 2 == 0 })
            .subscribe(observer)

        assertContentEquals(listOf(2, 4, "complete"), observer.received)
    }

    @Test
    fun `should forward errors`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        Observable.error<Int>()
            .apply(filter { it % 2 == 0 })
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}