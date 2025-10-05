package de.atennert.rx

import de.atennert.rx.operators.last
import de.atennert.rx.operators.mergeWith

fun interface Subscribe<T> {
    fun subscribe(subscriber: Subscriber<T>): Subscription
}

open class Observable<T>(private val subscribeFn: Subscribe<T>) {

    fun <R> apply(operator: Operator<T, R>): Observable<R> {
        return operator.call(this)
    }

    fun subscribe(observer: Observer<T>): Subscription {
        return subscribeFn.subscribe(Subscriber(observer))
    }

    companion object {
        private val EMPTY: Observable<Any?> = Observable { subscriber ->
            subscriber.complete()
            Subscription { subscriber.unsubscribe() }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): Observable<T> = EMPTY as Observable<T>

        fun <T> error(error: Throwable = Error()) = Observable<T> { subscriber ->
            subscriber.error(error)
            Subscription { subscriber.unsubscribe() }
        }

        fun <T> of(vararg elems: T): Observable<T> {
            return Observable { subscriber ->
                elems.forEach { subscriber.next(it) }
                subscriber.complete()
                Subscription { subscriber.unsubscribe() }
            }
        }

        fun <T> merge(vararg obss: Observable<T>): Observable<T> {
            if (obss.isEmpty()) {
                return empty()
            }
            return obss[0].apply(mergeWith(*obss.drop(1).toTypedArray()))
        }

        fun <T> forkJoin(obss: List<Observable<T>>): Observable<List<T>> {
            if (obss.isEmpty()) {
                return empty()
            }

            return Observable { subscriber ->
                val valueBuffer = mutableMapOf<Int, T>()
                val subscription = Subscription()

                obss.map { it.apply(last()) }
                    .forEachIndexed { index, obs ->
                        subscription.add(obs.subscribe(object : Observer<T> {
                            override fun next(value: T) {
                                valueBuffer[index] = value
                            }

                            override fun error(error: Throwable) {
                                subscriber.error(error)
                                subscription.unsubscribe()
                            }

                            override fun complete() {
                                if (valueBuffer.size == obss.size) {
                                    val values = valueBuffer.keys
                                        .sorted()
                                        .map { valueBuffer.getValue(it) }

                                    subscriber.next(values)
                                    subscriber.complete()
                                }
                            }
                        }
                        ))
                    }
                subscription.add(subscriber)

                subscription
            }
        }
    }
}
