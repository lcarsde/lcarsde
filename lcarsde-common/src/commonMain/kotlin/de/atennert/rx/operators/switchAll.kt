package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription

fun <X> switchAll(): Operator<Observable<X>, X> {
    return Operator { source ->
        Observable { subscriber ->
            var currentValueSubscription: Subscription? = null

            source.subscribe(object : Observer<Observable<X>> {
                override fun next(value: Observable<X>) {
                    currentValueSubscription?.unsubscribe()

                    currentValueSubscription = value.subscribe(object : Observer<X> {
                        override fun next(value: X) {
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

                override fun error(error: Throwable) {
                    subscriber.error(error)
                }

                override fun complete() {
                    subscriber.complete()
                }
            })
        }
    }
}