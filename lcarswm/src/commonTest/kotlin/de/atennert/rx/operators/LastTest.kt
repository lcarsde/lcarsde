package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscriber
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test
import kotlin.test.assertIs

class LastTest {
    @Test
    fun `return nothing on nothing`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }

        Observable.empty<Int>()
            .apply(last())
            .subscribe(observer)

        observer.received.shouldContainExactly("complete")
    }

    @Test
    fun `return last when there is something`() {
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
            .apply(last())
            .subscribe(observer)

        observer.received.shouldContainExactly(3, "complete")
    }

    @Test
    fun `return error when there is error`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }

            override fun error(error: Throwable) {
                received.add(error)
            }
        }

        Observable.error<Int>()
            .apply(last())
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }
}