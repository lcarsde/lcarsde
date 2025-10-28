package de.atennert.rx.operators

import de.atennert.rx.*

fun <X, S : Any?, T : Any?> bufferWhile(bufferObs: Observable<S>, passThroughObs: Observable<T>): Operator<X, List<X>> {
    return Operator { source ->
        Observable { subscriber ->
            val values = mutableListOf<X>()
            var useBuffer = false

            val subscription = Subscription()

            subscription.add(subscriber)
            subscription.add(bufferObs.subscribe(NextObserver {
                useBuffer = true
            }))
            subscription.add(passThroughObs.subscribe(NextObserver {
                useBuffer = false
                subscriber.next(values.toList())
                values.clear()
            }))

            subscription.add(source.subscribe(object : Observer<X> {
                override fun next(value: X) {
                    if (useBuffer) {
                        values.add(value)
                    } else {
                        subscriber.next(listOf(value))
                    }
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                }

                override fun complete() {
                    subscriber.complete()
                    subscription.unsubscribe()
                }
            }))

            subscription
        }
    }
}