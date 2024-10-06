package de.atennert.rx

import kotlin.reflect.KProperty

class ReplaySubject<T>(private val replayCount: Int) : Subject<T>() {
    private val values = mutableListOf<T>()

    /**
     * @throws NoSuchElementException when no value has been set yet
     */
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        error?.let { throw it }
        return values.last()
    }

    override fun subscribe(observer: Observer<T>): Subscription {
        val subscription = super.subscribe(observer)
        if (!stopped) {
            values.forEach { observer.next(it) }
        }
        return subscription
    }

    override fun next(value: T) {
        super.next(value)
        addValue(value)
    }

    private fun addValue(value: T) {
        values.add(value)
        if (values.size > replayCount) {
            values.removeFirst()
        }
    }
}