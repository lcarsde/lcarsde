package de.atennert.rx.operators

import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals

class BufferWhileTest {
    @Test
    fun `should buffer values while buffering active and let values through otherwise`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val triggerBuffer = Subject<Int>()
        val triggerPassThrough = Subject<Int>()
        val source = Subject<Int>()
        source
            .apply(bufferWhile(triggerBuffer.asObservable(), triggerPassThrough.asObservable()))
            .subscribe(observer)

        source.next(1)

        assertContentEquals(listOf(listOf(1)), observer.received)

        triggerBuffer.next(1)

        source.next(2)
        source.next(3)

        assertContentEquals(listOf(listOf(1)), observer.received)

        triggerPassThrough.next(1)

        assertContentEquals(listOf(listOf(1), listOf(2, 3)), observer.received)

        source.next(4)

        assertContentEquals(listOf(listOf(1), listOf(2, 3), listOf(4)), observer.received)
    }
}