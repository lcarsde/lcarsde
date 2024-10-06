package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import de.atennert.rx.util.Tuple1
import de.atennert.rx.util.Tuple2
import de.atennert.rx.util.Tuple3
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CombineLatestWithTest {
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
            .apply(combineLatestWith())
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
        Observable.of(1)
            .apply(combineLatestWith(Observable.of(2)))
            .subscribe(observer)

        assertContentEquals(listOf(Tuple2(1, 2), "complete"), observer.received)
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
        Observable.of(1)
            .apply(combineLatestWith(Observable.of(2), Observable.of(3)))
            .subscribe(observer)

        assertContentEquals(listOf(Tuple3(1, 2, 3), "complete"), observer.received)
    }
}