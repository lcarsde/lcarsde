package de.atennert.rx

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertFails

class BehaviorSubjectTest {
    @Test
    fun `should return the initial value`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = BehaviorSubject(1)

        class Test {
            val value: Int by subject
        }

        subject.subscribe(observer)
        observer.received.shouldContainExactly(1)

        Test().value shouldBe 1
    }

    @Test
    fun `should return the updated value via next`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = BehaviorSubject(1)
        subject.next(2)

        class Test {
            val value: Int by subject
        }

        subject.subscribe(observer)
        observer.received.shouldContainExactly(2)

        Test().value shouldBe 2
    }

    @Test
    fun `should return the updated value via delegate`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = BehaviorSubject(1)

        class Test {
            var value: Int by subject
        }
        val test = Test()
        test.value = 2

        subject.subscribe(observer)
        observer.received.shouldContainExactly(2)

        test.value shouldBe 2
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
        val subject = BehaviorSubject(1)
        subject.error(error)

        class Test {
            val value: Int by subject
        }
        subject.subscribe(observer)

        observer.received.shouldContainExactly(error)

        assertFails { Test().value }
    }
}