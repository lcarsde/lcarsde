package de.atennert.rx

import kotlin.reflect.KProperty

class BehaviorSubject<T>(initial: T) : Subject<T>() {
    private var value = initial

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        error?.let { throw it }
        return value
    }

    override fun subscribe(observer: Observer<T>): Subscription {
        val subscription = super.subscribe(observer)
        if (!stopped) {
            observer.next(value)
        }
        return subscription
    }

    override fun next(value: T) {
        super.next(value)
        this.value = value
    }
}