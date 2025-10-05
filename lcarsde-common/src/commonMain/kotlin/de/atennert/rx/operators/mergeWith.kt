package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription

fun <X> mergeWith(vararg obss: Observable<X>): Operator<X, X> {
    return Operator { source ->
        Observable { subscriber ->
            val subscription = Subscription()
            var complete = obss.size + 1 // for source

            for (obs in listOf(source).plus(obss)) {
                obs.subscribe(object : Observer<X> {
                    override fun next(value: X) {
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
    }
}