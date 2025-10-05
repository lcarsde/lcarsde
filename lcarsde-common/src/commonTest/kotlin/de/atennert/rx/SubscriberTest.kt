package de.atennert.rx

import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SubscriberTest {
    @Test
    fun `should forward value`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.next(1)

        observer.received.shouldContainExactly(1)
    }

    @Test
    fun `should forward complete`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun complete() {
                received.add("complete")
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.complete()

        observer.received.shouldContainExactly("complete")
    }

    @Test
    fun `should forward error`() {
        val error = IllegalStateException("waaahahhh")
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.error(error)

        observer.received.shouldContainExactly(error)
    }

    @Test
    fun `should stop on complete`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.next(1)
        subscriber.complete()
        subscriber.next(2)

        observer.received.shouldContainExactly(1)

    }

    @Test
    fun `should stop on error`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.next(1)
        subscriber.error(IllegalStateException("waaaaaahhh"))
        subscriber.next(2)

        observer.received.shouldContainExactly(1)

    }

    @Test
    fun `should stop on unsubscribe`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subscriber = Subscriber(observer)

        subscriber.next(1)
        subscriber.unsubscribe()
        subscriber.next(2)

        observer.received.shouldContainExactly(1)

    }
}