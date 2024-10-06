package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import de.atennert.rx.util.Tuple1
import de.atennert.rx.util.Tuple2
import de.atennert.rx.util.Tuple3
import kotlin.test.Test
import kotlin.test.assertContentEquals

class WithLatestFromTest {
    @Test
    fun `combine 1 observable`() {
        val observer = object : Subscriber<Tuple1<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: Tuple1<Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1)
            .apply(withLatestFrom())
            .subscribe(observer)

        assertContentEquals(listOf(Tuple1(1), "complete"), observer.received)
    }

    @Test
    fun `combine 2 observables`() {
        val observer = object : Subscriber<Tuple2<Int, Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: Tuple2<Int, Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2)
            .apply(withLatestFrom(Observable.of(3, 4)))
            .subscribe(observer)

        assertContentEquals(listOf(Tuple2(1, 4), Tuple2(2, 4), "complete"), observer.received)
    }

    @Test
    fun `combine 3 observables`() {
        val observer = object : Subscriber<Tuple3<Int, Int, Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: Tuple3<Int, Int, Int>) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        Observable.of(1, 2)
            .apply(withLatestFrom(Observable.of(3, 4), Observable.of(5, 6)))
            .subscribe(observer)

        assertContentEquals(listOf(Tuple3(1, 4, 6), Tuple3(2, 4, 6), "complete"), observer.received)
    }
}