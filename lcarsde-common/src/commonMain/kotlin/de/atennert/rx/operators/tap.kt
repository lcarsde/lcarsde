package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable
import de.atennert.rx.ValueObserver

fun <T> Subscribable<T>.tap(f: (T) -> Unit) = Observable { subscriber ->
    this.subscribe(object : ValueObserver<T, T>(subscriber) {
        override fun next(value: T) {
            f(value)
            subscriber.next(value)
        }
    })
}
