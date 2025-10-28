package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class FilterNotNullTest {
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
        Observable.of(null, 2, null, 4)
            .apply(filterNotNull())
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
        Observable.error<Int?>()
            .apply(filterNotNull())
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}