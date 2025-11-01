package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Subscribable
import de.atennert.rx.Subscription

fun <T> Subscribable<T>.last() = Observable { subscriber ->
    var lastValue: T? = null
    var initialized = false

    val subscription = Subscription()
    subscription.add(this.subscribe(object : Observer<T> {
        override fun next(value: T) {
            lastValue = value
            initialized = true
        }

        override fun error(error: Throwable) {
            subscriber.error(error)
        }

        override fun complete() {
            if (initialized) {
                @Suppress("UNCHECKED_CAST")
                subscriber.next(lastValue as T)
            }
            subscriber.complete()
        }
    }))
    subscription.add(subscriber)

    subscription
}