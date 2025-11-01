package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable
import de.atennert.rx.ValueObserver

fun <T> Subscribable<T>.take(count: Int) = Observable { subscriber ->
    this.subscribe(object : ValueObserver<T, T>(subscriber) {
        var nextValueCount = 0

        override fun next(value: T) {
            subscriber.next(value)

            nextValueCount++
            if (nextValueCount >= count) {
                complete()
            }
        }
    })
}
