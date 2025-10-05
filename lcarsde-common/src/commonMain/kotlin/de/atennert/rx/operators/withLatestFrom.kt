package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription
import de.atennert.rx.util.*

private class WithSubscription<T>(obs: Observable<T>, private val onError: (Throwable) -> Unit) : Subscription() {
    var initialized = false
        private set

    var lastValue: T? = null
        private set

    inner class WithObserver : Observer<T> {
        override fun next(value: T) {
            initialized = true
            lastValue = value
        }

        override fun error(error: Throwable) = onError(error)

        override fun complete() {
            // Nothing to do
        }
    }
    val subscription = obs.subscribe(WithObserver())

    override fun unsubscribe() = subscription.unsubscribe()
}

fun <A> withLatestFrom(): Operator<A, Tuple1<A>> = Operator { source ->
    source.apply(map { Tuple(it) })
}

fun <A, B> withLatestFrom(obs1: Observable<B>): Operator<A, Tuple2<A, B>> {
    return Operator { source ->
        Observable { subscriber ->
            val withSubs = Tuple(
                WithSubscription(obs1, subscriber::error),
            )

            val subscription = Subscription()
            val withSubList = withSubs.toList()

            withSubList.forEach(subscription::add)

            subscription.add(source.subscribe(object : Observer<A> {
                override fun next(value: A) {
                    if (withSubList.any { !it.initialized }) {
                        return
                    }
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(Tuple(value, withSubs.v1.lastValue as B))
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                }

                override fun complete() {
                    subscriber.complete()
                }
            }))
            subscription.add(subscriber)
            subscription
        }
    }
}

fun <A, B, C> withLatestFrom(obs1: Observable<B>, obs2: Observable<C>): Operator<A, Tuple3<A, B, C>> {
    return Operator { source ->
        Observable { subscriber ->
            val withSubs = Tuple(
                WithSubscription(obs1, subscriber::error),
                WithSubscription(obs2, subscriber::error),
            )

            val subscription = Subscription()
            val withSubList = withSubs.toList().map { it as WithSubscription<*> }

            withSubList.forEach(subscription::add)

            subscription.add(source.subscribe(object : Observer<A> {
                override fun next(value: A) {
                    if (withSubList.any { !it.initialized }) {
                        return
                    }
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(Tuple(value, withSubs.v1.lastValue as B, withSubs.v2.lastValue as C))
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                }

                override fun complete() {
                    subscriber.complete()
                }
            }))
            subscription.add(subscriber)
            subscription
        }
    }
}

fun <A, B, C, D> withLatestFrom(obs1: Observable<B>, obs2: Observable<C>, obs3: Observable<D>): Operator<A, Tuple4<A, B, C, D>> {
    return Operator { source ->
        Observable { subscriber ->
            val withSubs = Tuple(
                WithSubscription(obs1, subscriber::error),
                WithSubscription(obs2, subscriber::error),
                WithSubscription(obs3, subscriber::error),
            )

            val subscription = Subscription()
            val withSubList = withSubs.toList().map { it as WithSubscription<*> }

            withSubList.forEach(subscription::add)

            subscription.add(source.subscribe(object : Observer<A> {
                override fun next(value: A) {
                    if (withSubList.any { !it.initialized }) {
                        return
                    }
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(Tuple(value, withSubs.v1.lastValue as B, withSubs.v2.lastValue as C, withSubs.v3.lastValue as D))
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                }

                override fun complete() {
                    subscriber.complete()
                }
            }))
            subscription.add(subscriber)
            subscription
        }
    }
}
