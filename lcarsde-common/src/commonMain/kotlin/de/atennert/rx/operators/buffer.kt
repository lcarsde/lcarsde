package de.atennert.rx.operators

import de.atennert.rx.*

fun <T, U : Any?> Subscribable<T>.buffer(triggerObs: Subscribable<U>) = Observable { subscriber ->
    val values = mutableListOf<T>()
    var complete = false

    val subscription = Subscription()

    subscription.add(subscriber)
    subscription.add(triggerObs.subscribe(NextObserver {
        subscriber.next(values.toList())
        values.clear()
        if (complete) {
            subscriber.complete()
        }
    }))

    subscription.add(this.subscribe(object : Observer<T> {
        override fun next(value: T) {
            values.add(value)
        }

        override fun error(error: Throwable) {
            subscriber.error(error)
        }

        override fun complete() {
            complete = true
        }
    }))

    subscription
}
