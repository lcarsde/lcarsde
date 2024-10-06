package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subject
import de.atennert.rx.Subscriber
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SwitchAllTest {
    @Test
    fun `should switch observables`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun complete() {
                received.add("complete")
            }
        }
        val sub1 = Subject<Int>()
        val sub2 = Subject<Int>()
        val parentSub = Subject<Observable<Int>>()

        parentSub.asObservable()
            .apply(switchAll())
            .subscribe(observer)

        parentSub.next(sub1.asObservable())

        sub1.next(1)
        sub1.next(2)

        parentSub.next(sub2.asObservable())

        sub2.next(10)

        sub1.next(3)
        sub1.complete()

        sub2.next(20)
        sub2.next(30)
        sub2.complete()

        assertContentEquals(listOf(1, 2, 10, 20, 30), observer.received)
    }

    @Test
    fun `should forward errors`() {
        val observer = object : Subscriber<Int>() {
            val received = mutableListOf<Any>()
            override fun next(value: Int) {
                received.add(value)
            }
            override fun error(error: Throwable) {
                received.add(error)
            }
        }
        val error = Error("Whoops")
        val sub1 = Subject<Int>()
        val sub2 = Subject<Int>()
        val parentSub = Subject<Observable<Int>>()

        parentSub.asObservable()
            .apply(switchAll())
            .subscribe(observer)

        parentSub.next(sub1.asObservable())

        sub1.next(1)
        sub1.error(error)

        parentSub.next(sub2.asObservable())

        sub2.next(10)
        sub2.complete()

        assertContentEquals(listOf(1, error), observer.received)
    }
}