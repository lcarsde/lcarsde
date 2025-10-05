package de.atennert.rx

import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test
import kotlin.test.assertFails

class SubjectTest {
    @Test
    fun `Subject should deliver values`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        subject.subscribe(observer)

        subject.next(1)
        subject.next(2)

        observer.received.shouldContainExactly(1, 2)
    }

    @Test
    fun `Subject should complete`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun complete() {
                received.add("complete")
            }
        }
        val subject = Subject<Int>()
        subject.subscribe(observer)

        subject.complete()

        observer.received.shouldContainExactly("complete")
    }

    @Test
    fun `Subject should error`() {
        val error = IllegalStateException("waaaahhhhh")
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val subject = Subject<Int>()
        subject.subscribe(observer)

        subject.error(error)

        observer.received.shouldContainExactly(error)
    }

    @Test
    fun `should work as observable`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun complete() {
                received.add("complete")
            }
        }
        val subject = Subject<Int>()
        subject.asObservable().subscribe(observer)

        subject.complete()

        observer.received.shouldContainExactly("complete")
    }

    @Test
    fun `should take operators`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("completed")
            }
        }
        val subject = Subject<Int>()
        subject.apply { observable ->
                Observable { subscriber ->
                    (observable).subscribe(object : Subscriber<Int>() {
                        override fun next(value: Int) {
                            subscriber.next(value * 2)
                        }
                        override fun complete() {
                            subscriber.complete()
                        }
                    })
                }
            }
            .subscribe(observer)
        subject.next(1)
        subject.next(2)
        subject.complete()

        observer.received.shouldContainExactly(2, 4, "completed")
    }

    @Test
    fun `shouldn't send values after unsubscribe`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val subscription = subject.subscribe(observer)

        subject.next(1)
        subscription.unsubscribe()
        subject.next(2)

        observer.received.shouldContainExactly(1)

    }

    @Test
    fun `should throw error on next after complete`() {
        val subject = Subject<Int>()
        subject.complete()
        assertFails { subject.next(0) }
    }

    @Test
    fun `should throw error on next after error`() {
        val subject = Subject<Int>()
        subject.error(IllegalStateException("waaaaahhhh"))
        assertFails { subject.next(0) }
    }

    @Test
    fun `send complete on subscribing to completed Subject`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun complete() {
                received.add("complete")
            }
        }
        val subject = Subject<Int>()
        subject.complete()
        subject.subscribe(observer)

        observer.received.shouldContainExactly("complete")
    }

    @Test
    fun `send error on subscribing to errored Subject`() {
        val error = IllegalStateException("waaaahhhh")
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val subject = Subject<Int>()
        subject.error(error)
        subject.subscribe(observer)

        observer.received.shouldContainExactly(error)
    }
}