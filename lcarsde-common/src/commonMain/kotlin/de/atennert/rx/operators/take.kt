package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator

fun <X> take(count: Int): Operator<X, X> {
    return Operator { source ->
        Observable { subscriber ->
            source.subscribe(object : Observer<X> {
                var nextValueCount = 0

                override fun next(value: X) {
                    subscriber.next(value)

                    nextValueCount++
                    if (nextValueCount >= count) {
                        complete()
                    }
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscriber.unsubscribe()
                }

                override fun complete() {
                    subscriber.complete()
                    subscriber.unsubscribe()
                }
            })
        }
    }
}