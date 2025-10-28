package de.atennert.rx.operators

import de.atennert.rx.*

fun <X> shareReplay(replayCount: Int = 1, refCount: Boolean = false): Operator<X, X> {
    return Operator { source ->
        val values = mutableListOf<X>()
        var isComplete = false
        val subscribers = mutableSetOf<Subscriber<X>>()

        var sourceSubscription: Subscription? = null

        fun addValue(value: X) {
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

        Observable { subscriber ->
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
                sourceSubscription = source.subscribe(object : Observer<X> {
                    override fun next(value: X) {
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
}
