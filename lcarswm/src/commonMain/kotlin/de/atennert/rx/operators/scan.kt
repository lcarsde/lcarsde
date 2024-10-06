package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator

fun <X, Y> scan(seed: Y, f: (acc: Y, value: X) -> Y): Operator<X, Y> {
    return Operator { source ->
        Observable { subscriber ->
            source.subscribe(object: Observer<X> {
                var acc = seed
                override fun next(value: X) {
                    acc = f(acc, value)
                    subscriber.next(acc)
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
