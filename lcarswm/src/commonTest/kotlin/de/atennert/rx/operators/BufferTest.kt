package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class BufferTest {
    @Test
    fun `should buffer values until trigger`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val trigger = Subject<Int>()
        val source = Subject<Int>()
        source
            .apply(buffer(trigger.asObservable()))
            .subscribe(observer)

        source.next(1)
        source.next(2)

        assertContentEquals(listOf(), observer.received)

        trigger.next(1)

        source.next(3)

        assertContentEquals(listOf(listOf(1, 2)), observer.received)

        source.next(4)
        trigger.next(1)

        assertContentEquals(listOf(listOf(1, 2), listOf(3, 4)), observer.received)
    }

    @Test
    fun `should buffer values until trigger with close`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val trigger = Subject<Int>()
        val source = Subject<Int>()
        source
            .apply(buffer(trigger.asObservable()))
            .subscribe(observer)

        source.next(1)
        source.next(2)

        assertContentEquals(listOf(), observer.received)

        trigger.next(1)

        source.next(3)
        source.complete()

        assertContentEquals(listOf(listOf(1, 2)), observer.received)

        trigger.next(1)

        assertContentEquals(listOf(listOf(1, 2), listOf(3), "complete"), observer.received)
    }

    @Test
    fun `should forward errors`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val trigger = Subject<Int>()
        Observable.error<Int>()
            .apply(buffer(trigger.asObservable()))
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}