package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable
import de.atennert.rx.ValueObserver

fun <T> Subscribable<T?>.filterNotNull() = Observable { subscriber ->
    this.subscribe(object : ValueObserver<T?, T>(subscriber) {
        override fun next(value: T?) {
            if (value != null) {
                subscriber.next(value)
            }
        }
    })
}
