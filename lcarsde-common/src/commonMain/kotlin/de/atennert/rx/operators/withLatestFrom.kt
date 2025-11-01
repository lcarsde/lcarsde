package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Subscribable
import de.atennert.rx.Subscription
import de.atennert.rx.util.Tuple
import de.atennert.rx.util.Tuple2
import de.atennert.rx.util.Tuple3

private class WithSubscription<T>(obs: Subscribable<T>, private val onError: (Throwable) -> Unit) : Subscription() {
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

fun <T> Subscribable<T>.withLatestFrom() = this.map { Tuple(it) }

fun <T1, T2> Subscribable<T1>.withLatestFrom(obs2: Subscribable<T2>): Observable<Tuple2<T1, T2>> =
    this.internalWithLatestFrom(obs2)
        .map { Tuple2(it) }

fun <T1, T2, T3> Subscribable<T1>.withLatestFrom(
    obs2: Subscribable<T2>,
    obs3: Subscribable<T3>
): Observable<Tuple3<T1, T2, T3>> =
    this.internalWithLatestFrom(obs2, obs3)
        .map { Tuple3(it) }

private fun Subscribable<*>.internalWithLatestFrom(vararg obss: Subscribable<*>) = Observable { subscriber ->
    val withSubs = obss.map { WithSubscription(it, subscriber::error) }
    val subscription = Subscription()
    withSubs.forEach(subscription::add)

    subscription.add(this.subscribe(object : Observer<Any?> {
        override fun next(value: Any?) {
            if (withSubs.any { !it.initialized }) {
                return
            }
            @Suppress("UNCHECKED_CAST")
            subscriber.next(listOf(value).plus(withSubs.map { it.lastValue }))
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
