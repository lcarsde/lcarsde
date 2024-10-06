package de.atennert.rx.operators

import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import de.atennert.rx.util.Tuple
import de.atennert.rx.util.Tuple2
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CombinationTest {
    @Test
    fun `combine buffer with withLatestFrom`() {
        val observer = object : Subscriber<Tuple2<List<Int>, Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: Tuple2<List<Int>, Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val subject1 = Subject<Int>()
        val subject2 = Subject<Int>()

        subject1.apply(buffer(subject2.asObservable()))
            .apply(withLatestFrom(subject2.asObservable()))
            .subscribe(observer)

        subject2.next(10)

        subject1.next(1)
        subject1.next(2)

        subject2.next(20)

        assertContentEquals(listOf(Tuple(listOf(), 10), Tuple(listOf(1, 2), 20)), observer.received)
    }
}