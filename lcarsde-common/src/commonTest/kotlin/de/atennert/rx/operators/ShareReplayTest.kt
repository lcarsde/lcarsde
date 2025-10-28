package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class ShareReplayTest {
    @Test
    fun `should forward values`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay())

        subject.next(1)

        obs.subscribe(observer)

        subject.next(2)

        assertContentEquals(listOf(2), observer.received)
    }

    @Test
    fun `should replay 1 value default`() {
        val preObserver = object : Subscriber<Int>() {}
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay())

        obs.subscribe(preObserver)

        subject.next(1)
        subject.next(2)
        subject.next(3)

        obs.subscribe(observer)

        assertContentEquals(listOf(3), observer.received)
    }

    @Test
    fun `should replay 2 values`() {
        val preObserver = object : Subscriber<Int>() {}
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2))

        obs.subscribe(preObserver)

        subject.next(1)
        subject.next(2)
        subject.next(3)

        obs.subscribe(observer)

        assertContentEquals(listOf(2, 3), observer.received)
    }

    @Test
    fun `should forward errors`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        Observable.error<Int>()
            .apply(map { it + 1 })
            .subscribe(observer)

        assertIs<Throwable>(observer.received[0])
    }

    @Test
    fun `should forward complete`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun complete() {
                received.add("complete")
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2))

        subject.complete()

        obs.subscribe(observer)
            .unsubscribe()

        assertContentEquals(listOf("complete"), observer.received)
    }

    @Test
    fun `should replay and forward complete`() {
        val preObserver = object : Subscriber<Int>() {}
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2))
        obs.subscribe(preObserver)

        subject.next(1)
        subject.next(2)
        subject.next(3)
        subject.complete()

        obs.subscribe(observer)

        assertContentEquals(listOf(2, 3, "complete"), observer.received)
    }

    @Test
    fun `should replay values for multiple observers`() {
        val preObserver = object : Subscriber<Int>() {}
        val observer1 = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val observer2 = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2))
        obs.subscribe(preObserver)

        subject.next(1)
        subject.next(2)
        subject.next(3)

        obs.subscribe(observer1)
        obs.subscribe(observer2)

        assertContentEquals(listOf(2, 3), observer1.received)
        assertContentEquals(listOf(2, 3), observer2.received)
    }

    @Test
    fun `should forget values on last unsubscribe with refCount true`() {
        val observer1 = object : Subscriber<Int>() {}
        val observer2 = object : Subscriber<Int>() {}
        val observer3 = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2, true))

        val sub1 = obs.subscribe(observer1)
        val sub2 = obs.subscribe(observer2)

        subject.next(1)
        subject.next(2)
        subject.next(3)

        sub1.unsubscribe()
        sub2.unsubscribe()

        obs.subscribe(observer3)

        assertContentEquals(listOf(), observer3.received)
    }

    @Test
    fun `should NOT forget values on last unsubscribe with refCount false default`() {
        val observer1 = object : Subscriber<Int>() {}
        val observer2 = object : Subscriber<Int>() {}
        val observer3 = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
        }
        val subject = Subject<Int>()
        val obs = subject
            .apply(shareReplay(2))

        val sub1 = obs.subscribe(observer1)
        val sub2 = obs.subscribe(observer2)

        subject.next(1)
        subject.next(2)
        subject.next(3)

        sub1.unsubscribe()
        sub2.unsubscribe()

        obs.subscribe(observer3)

        assertContentEquals(listOf(2, 3), observer3.received)
    }
}