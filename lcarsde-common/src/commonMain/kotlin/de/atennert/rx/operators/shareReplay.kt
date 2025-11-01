package de.atennert.rx.operators

import de.atennert.rx.*

fun <T> Subscribable<T>.shareReplay(replayCount: Int = 1, refCount: Boolean = false): Observable<T> {
    val values = mutableListOf<T>()
    var isComplete = false
    val subscribers = mutableSetOf<Subscriber<T>>()

    var sourceSubscription: Subscription? = null

    fun addValue(value: T) {
        values.add(value)
        if (values.size > replayCount) {
            values.removeFirst()
        }
    }

    fun unsubscribeAll() {
        subscribers.forEach { it.unsubscribe() }
        subscribers.clear()

        sourceSubscription?.unsubscribe()
        sourceSubscription = null
    }

    return Observable { subscriber ->
        if (!isComplete) {
            subscribers.add(subscriber)
        }

        if (sourceSubscription != null || isComplete) {
            values.forEach { subscriber.next(it) }
        }
        if (isComplete) {
            subscriber.complete()
        } else if (sourceSubscription == null) {
            values.clear()
            isComplete = false
            sourceSubscription = this.subscribe(object : Observer<T> {
                override fun next(value: T) {
                    addValue(value)
                    subscribers.forEach { it.next(value) }
                }

                override fun error(error: Throwable) {
                    subscribers.forEach { it.error(error) }
                    unsubscribeAll()
                }

                override fun complete() {
                    isComplete = true
                    subscribers.forEach { it.complete() }
                    unsubscribeAll()
                }
            })
        }

        Subscription {
            subscriber.unsubscribe()
            subscribers.remove(subscriber)

            if (refCount && subscribers.size < 1) {
                sourceSubscription?.unsubscribe()
                sourceSubscription = null
            }
        }
    }
}
