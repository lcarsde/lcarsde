package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Subscribable
import de.atennert.rx.Subscription

fun <T> Subscribable<T>.mergeWith(vararg obss: Subscribable<T>) = Observable { subscriber ->
    val subscription = Subscription()
    var complete = obss.size + 1 // for source

    for (obs in listOf(this).plus(obss)) {
        obs.subscribe(object : Observer<T> {
            override fun next(value: T) {
                subscriber.next(value)
            }

            override fun error(error: Throwable) {
                subscriber.error(error)
                subscription.unsubscribe()
            }

            override fun complete() {
                complete--
                if (complete <= 0) {
                    subscriber.complete()
                    subscription.unsubscribe()
                }
            }
        })
    }
    subscription.add(subscriber)

    subscription
}

