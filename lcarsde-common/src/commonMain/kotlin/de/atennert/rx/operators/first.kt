package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Subscribable
import de.atennert.rx.Subscription

fun <T> Subscribable<T>.first() = Observable { subscriber ->
    val subscription = Subscription()
    subscription.add(this.subscribe(object : Observer<T> {
        override fun next(value: T) {
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