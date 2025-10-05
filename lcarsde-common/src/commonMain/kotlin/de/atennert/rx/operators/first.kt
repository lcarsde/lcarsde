package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription

fun <A> first() = Operator { source ->
    Observable { subscriber ->
        val subscription = Subscription()
        subscription.add(source.subscribe(object : Observer<A> {
            override fun next(value: A) {
                subscriber.next(value)
                subscriber.complete()
            }

            override fun error(error: Throwable) {
                subscriber.error(error)
            }

            override fun complete() {
                subscriber.complete()
            }
        }))
        subscription.add(subscriber)

        subscription
    }
}