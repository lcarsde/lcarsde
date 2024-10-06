package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class TakeTest {
    @Test
    fun `should take a value`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2, 3)
            .apply(take(1))
            .subscribe(observer)

        assertContentEquals(listOf(1, "complete"), observer.received)
    }

    @Test
    fun `should take two values`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2, 3)
            .apply(take(2))
            .subscribe(observer)

        assertContentEquals(listOf(1, 2, "complete"), observer.received)
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
            .apply(take(1))
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}