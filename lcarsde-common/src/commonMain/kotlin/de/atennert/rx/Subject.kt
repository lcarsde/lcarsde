package de.atennert.rx

import kotlin.reflect.KProperty

open class Subject<T> : Observer<T> {
    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        throw UnsupportedOperationException("Use Behavior Subject to do that! ($thisRef.${property.name})")
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        next(value)
    }

    fun <R> apply(operator: Operator<T, R>): Observable<R> {
        return asObservable().apply(operator)
    }

    private val subscribers = mutableSetOf<Subscriber<T>>()

    open fun subscribe(observer: Observer<T>): Subscription {
        if (stopped) {
            val err = error
            if (err != null) {
                observer.error(err)
            } else {
                observer.complete()
            }
            return Subscription { }
        }
        val subscriber = Subscriber(observer)
        subscribers.add(subscriber)
        return Subscription {
            subscribers.remove(subscriber)
            subscriber.unsubscribe()
        }
    }

    protected var stopped = false
    protected var error: Throwable? = null

    override fun next(value: T) {
        if (stopped) {
            throw IllegalStateException("Can't submit a value after subject is finished")
        }
        subscribers.forEach { it.next(value) }
    }

    override fun error(error: Throwable) {
        if (stopped) {
            throw IllegalStateException("Can't submit an error after subject is finished")
        }
        this.error = error
        subscribers.forEach { it.error(error) }
        stopped = true
    }

    override fun complete() {
        if (stopped) {
            throw IllegalStateException("Can't submit complete after subject is finished")
        }
        subscribers.forEach(Subscriber<T>::complete)
        stopped = true
    }

    fun asObservable(): Observable<T> {
        return Observable { subscribe(it) }
    }
}