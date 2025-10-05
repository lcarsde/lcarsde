package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class MergeWithTest {
    @Test
    fun `merge with 0 observables`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val sj1 = Subject<Int>()
        sj1.apply(mergeWith())
            .subscribe(observer)
        sj1.next(1)
        sj1.next(2)
        sj1.complete()

        assertContentEquals(listOf(1, 2, "complete"), observer.received)
    }

    @Test
    fun `merge 2 observables`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val sj1 = Subject<Int>()
        val sj2 = Subject<Int>()
        val sj3 = Subject<Int>()
        sj1.apply(mergeWith(sj2.asObservable(), sj3.asObservable()))
            .subscribe(observer)
        sj1.next(1)
        sj3.next(21)
        sj2.next(11)
        sj1.next(2)
        sj2.next(12)
        sj1.complete()

        assertContentEquals(listOf(1, 21, 11, 2, 12), observer.received)
    }

    @Test
    fun `complete when all completed`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val sj1 = Subject<Int>()
        val sj2 = Subject<Int>()
        val sj3 = Subject<Int>()
        sj1.apply(mergeWith(sj2.asObservable(), sj3.asObservable()))
            .subscribe(observer)

        sj1.complete()
        assertContentEquals(emptyList(), observer.received)

        sj2.complete()
        assertContentEquals(emptyList(), observer.received)

        sj3.complete()
        assertContentEquals(listOf("complete"), observer.received)
    }

    @Test
    fun `error on source error`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        Observable.error<Int>()
            .apply(mergeWith(Observable.of(1)))
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }

    @Test
    fun `error on other error`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        Observable.of(1)
            .apply(mergeWith(Observable.error()))
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}