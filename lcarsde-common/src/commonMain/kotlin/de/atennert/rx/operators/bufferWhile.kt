package de.atennert.rx.operators

import de.atennert.rx.*

fun <T, U : Any?, V : Any?> Subscribable<T>.bufferWhile(
    bufferObs: Subscribable<U>,
    passThroughObs: Subscribable<V>
): Observable<List<T>> = Observable { subscriber ->
    val values = mutableListOf<T>()
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

    subscription.add(this.subscribe(object : Observer<T> {
        override fun next(value: T) {
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
