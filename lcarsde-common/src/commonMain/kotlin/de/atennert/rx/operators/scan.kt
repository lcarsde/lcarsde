package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable
import de.atennert.rx.ValueObserver

fun <T, U> Subscribable<T>.scan(seed: U, f: (acc: U, value: T) -> U) = Observable { subscriber ->
    this.subscribe(object : ValueObserver<T, U>(subscriber) {
        var acc = seed

        override fun next(value: T) {
            acc = f(acc, value)
            subscriber.next(acc)
        }
    })
}
