package de.atennert.rx

import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class ObservableTest {
    @Test
    fun `should provide value and close with Subscriber`() {
        val observer = object : Subscriber<Number>() {
            val received = mutableListOf<Any>()
            override fun next(value: Number) {
                received.add(value)
            }

            override fun complete() {
                received.add("completed")
            }
        }
        Observable.of<Number>(1, 2)
            .subscribe(observer)

        observer.received.shouldContainExactly(1, 2, "completed")
    }

    /*######################################*
     * FORK JOIN
     *######################################*/

    @Test
    fun `fork join nothing just completes`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }

            override fun complete() {
                received.add("completed")
            }
        }
        Observable.forkJoin<Int>(emptyList())
            .subscribe(observer)

        observer.received.shouldContainExactly("completed")
    }

    @Test
    fun `fork join one observable gives the observables last value`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }

            override fun complete() {
                received.add("completed")
            }
        }
        Observable.forkJoin(listOf(Observable.of(1, 2, 3)))
            .subscribe(observer)

        observer.received.shouldContainExactly(listOf(3), "completed")
    }

    @Test
    fun `fork join two observables`() {
        val observer = object : Subscriber<List<Int>>() {
            val received = mutableListOf<Any>()
            override fun next(value: List<Int>) {
                received.add(value)
            }

            override fun complete() {
                received.add("completed")
            }
        }
        Observable.forkJoin(
            listOf(
                Observable.of(1, 2, 3),
                Observable.of(10, 20, 30, 40),
            )
        )
            .subscribe(observer)

        observer.received.shouldContainExactly(listOf(3, 40), "completed")
    }
}