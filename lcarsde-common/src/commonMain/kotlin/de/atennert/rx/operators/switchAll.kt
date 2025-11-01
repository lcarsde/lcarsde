package de.atennert.rx.operators

import de.atennert.rx.*

fun <X : Subscribable<T>, T> Subscribable<X>.switchAll() = Observable { subscriber ->
    var currentValueSubscription: Subscription? = null
    this.subscribe(object : ValueObserver<X, T>(subscriber) {
        override fun next(value: X) {
            currentValueSubscription?.unsubscribe()

            currentValueSubscription = value.subscribe(object : Observer<T> {
                override fun next(value: T) {
                    subscriber.next(value)
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    currentValueSubscription?.unsubscribe()
                    currentValueSubscription = null
                }

                override fun complete() {
                    // Nothing to do
                }
            })
        }
    })
}