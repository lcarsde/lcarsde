package de.atennert.rx

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertFails

class ReplaySubjectTest {
    @Test
    fun `should return the last N values via next`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = ReplaySubject<Int>(2)
        class Test {
            var value: Int by subject
        }

        subject.next(1)
        subject.next(2)
        subject.next(3)

        subject.subscribe(observer)

        observer.received.shouldContainExactly(2, 3)
        Test().value shouldBe 3
    }

    @Test
    fun `should return the last N values via delegate`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = ReplaySubject<Int>(2)
        class Test {
            var value: Int by subject
        }

        Test().value = 1
        Test().value = 2
        Test().value = 3

        subject.subscribe(observer)

        observer.received.shouldContainExactly(2, 3)
        Test().value shouldBe 3
    }

    @Test
    fun `should throw given error on late connect`() {
        val error = IllegalStateException("wahahaha")
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val subject = ReplaySubject<Int>(1)
        subject.next(2)
        subject.error(error)

        class Test {
            val value: Int by subject
        }

        subject.subscribe(observer)

        observer.received.shouldContainExactly(error)

        assertFails { Test().value }
    }
}