package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Subscribable
import de.atennert.rx.Subscription
import de.atennert.rx.util.Tuple
import de.atennert.rx.util.Tuple1
import de.atennert.rx.util.Tuple2
import de.atennert.rx.util.Tuple3

fun <T> Subscribable<T>.combineLatestWith(): Observable<Tuple1<T>> = this.map { Tuple(it) }

fun <T1, T2> Subscribable<T1>.combineLatestWith(obs2: Subscribable<T2>): Observable<Tuple2<T1, T2>> =
    this.internalCombineLatestWith(obs2)
        .map { Tuple2(it) }

fun <T1, T2, T3> Subscribable<T1>.combineLatestWith(
    o2: Subscribable<T2>,
    o3: Subscribable<T3>
): Subscribable<Tuple3<T1, T2, T3>> =
    this.internalCombineLatestWith(o2, o3)
        .map { Tuple3(it) }

private fun Subscribable<*>.internalCombineLatestWith(vararg obss: Subscribable<*>) = Observable { subscriber ->
    val values = arrayOfNulls<Any?>(obss.size + 1)
    val isValueSet = BooleanArray(obss.size + 1) { false }
    val isObsComplete = BooleanArray(obss.size + 1) { false }
    val subscription = Subscription()

    fun nextAll() {
        if (isValueSet.all { it }) {
            subscriber.next(values)
        }
    }

    fun completeAll() {
        if (isObsComplete.all { it }) {
            subscriber.complete()
            subscription.unsubscribe()
        }
    }

    listOf(this).plus(obss).forEachIndexed { i, obs ->
        subscription.add(obs.subscribe(object : Observer<Any?> {
            override fun next(value: Any?) {
                values[i] = value
                isValueSet[i] = true
                nextAll()
            }

            override fun error(error: Throwable) {
                subscriber.error(error)
                subscription.unsubscribe()
            }

            override fun complete() {
                isObsComplete[i] = true
                completeAll()
            }
        }))
    }

    subscription
}
