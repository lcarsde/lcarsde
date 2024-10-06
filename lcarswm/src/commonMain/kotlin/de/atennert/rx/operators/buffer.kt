package de.atennert.rx.operators

import de.atennert.rx.*

fun <X, T : Any?> buffer(triggerObs: Observable<T>): Operator<X, List<X>> {
    return Operator { source ->
        Observable { subscriber ->
            val values = mutableListOf<X>()
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

            subscription.add(source.subscribe(object : Observer<X> {
                override fun next(value: X) {
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
    }
}