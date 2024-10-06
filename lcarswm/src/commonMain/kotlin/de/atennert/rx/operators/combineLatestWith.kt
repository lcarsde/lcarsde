package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Observer
import de.atennert.rx.Operator
import de.atennert.rx.Subscription
import de.atennert.rx.util.Tuple
import de.atennert.rx.util.Tuple1
import de.atennert.rx.util.Tuple2
import de.atennert.rx.util.Tuple3

fun <X> combineLatestWith(): Operator<X, Tuple1<X>> = Operator { source ->
    source.apply(map { Tuple(it) })
}

fun <A, B> combineLatestWith(obs2: Observable<B>): Operator<A, Tuple2<A, B>> {
    return Operator { obs1 ->
        Observable { subscriber ->
            var value1: A? = null
            var value2: B? = null
            var isValue1Set = false
            var isValue2Set = false
            var isObs1Complete = false
            var isObs2Complete = false
            val subscription = Subscription()

            fun nextAll() {
                if (isValue1Set && isValue2Set) {
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(Tuple(value1 as A, value2 as B))
                }
            }

            fun completeAll() {
                if (isObs1Complete && isObs2Complete) {
                    subscriber.complete()
                    subscription.unsubscribe()
                }
            }

            subscription.add(obs1.subscribe(object : Observer<A> {
                override fun next(value: A) {
                    value1 = value
                    isValue1Set = true
                    nextAll()
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscription.unsubscribe()
                }

                override fun complete() {
                    isObs1Complete = true
                    completeAll()
                }
            }))
            subscription.add(obs2.subscribe(object : Observer<B> {
                override fun next(value: B) {
                    value2 = value
                    isValue2Set = true
                    nextAll()
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscription.unsubscribe()
                }

                override fun complete() {
                    isObs2Complete = true
                    completeAll()
                }
            }))

            subscription
        }
    }
}

fun <A, B, C> combineLatestWith(obs2: Observable<B>, obs3: Observable<C>): Operator<A, Tuple3<A, B, C>> {
    return Operator { obs1 ->
        Observable { subscriber ->
            var value1: A? = null
            var value2: B? = null
            var value3: C? = null
            var isValue1Set = false
            var isValue2Set = false
            var isValue3Set = false
            var isObs1Complete = false
            var isObs2Complete = false
            var isObs3Complete = false
            val subscription = Subscription()

            fun nextAll() {
                if (isValue1Set && isValue2Set && isValue3Set) {
                    @Suppress("UNCHECKED_CAST")
                    subscriber.next(Tuple(value1 as A, value2 as B, value3 as C))
                }
            }

            fun completeAll() {
                if (isObs1Complete && isObs2Complete && isObs3Complete) {
                    subscriber.complete()
                    subscription.unsubscribe()
                }
            }

            subscription.add(obs1.subscribe(object : Observer<A> {
                override fun next(value: A) {
                    value1 = value
                    isValue1Set = true
                    nextAll()
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscription.unsubscribe()
                }

                override fun complete() {
                    isObs1Complete = true
                    completeAll()
                }
            }))
            subscription.add(obs2.subscribe(object : Observer<B> {
                override fun next(value: B) {
                    value2 = value
                    isValue2Set = true
                    nextAll()
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscription.unsubscribe()
                }

                override fun complete() {
                    isObs2Complete = true
                    completeAll()
                }
            }))
            subscription.add(obs3.subscribe(object : Observer<C> {
                override fun next(value: C) {
                    value3 = value
                    isValue3Set = true
                    nextAll()
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscription.unsubscribe()
                }

                override fun complete() {
                    isObs3Complete = true
                    completeAll()
                }
            }))

            subscription
        }
    }
}

