package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class SwitchMapTest {
    @Test
    fun `should map values`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2)
            .apply(switchMap { Observable { subscriber ->
                subscriber.next(1 + it * 10)
                subscriber.next(2 + it * 10)
                subscriber.complete()
                subscriber
            } })
            .subscribe(observer)

        assertContentEquals(listOf(11, 12, 21, 22, "complete"), observer.received)
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
            .apply(switchMap { Observable { subscriber ->
                subscriber.next(1 + it * 10)
                subscriber.next(2 + it * 10)
                subscriber.complete()
                subscriber
            } })
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}