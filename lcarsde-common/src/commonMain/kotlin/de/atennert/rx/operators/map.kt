package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable
import de.atennert.rx.ValueObserver

fun <T, U> Subscribable<T>.map(f: (T) -> U) = Observable { subscriber ->
    this.subscribe(object : ValueObserver<T, U>(subscriber) {
        override fun next(value: T) {
            subscriber.next(f(value))
        }
    })
}
