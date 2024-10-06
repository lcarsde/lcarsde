package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription

fun <A> last() = Operator<A, A> { source ->
    Observable { subscriber ->
        var lastValue: A? = null
        var initialized = false

        val subscription = Subscription()
        subscription.add(source.subscribe(object : Observer<A> {
            override fun next(value: A) {
                lastValue = value
                initialized = true
            }

            override fun error(error: Throwable) {
                subscriber.error(error)
            }

            override fun complete() {
                if (initialized) {
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(lastValue as A)
                }
                subscriber.complete()
            }
        }))
        subscription.add(subscriber)

        subscription
    }
}