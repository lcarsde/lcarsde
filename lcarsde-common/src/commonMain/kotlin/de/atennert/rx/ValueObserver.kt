package de.atennert.rx

abstract class ValueObserver<T, U>(private val subscriber: Subscriber<U>) : Observer<T> {
    override fun error(error: Throwable) {
        subscriber.error(error)
        subscriber.unsubscribe()
    }

    override fun complete() {
        subscriber.complete()
        subscriber.unsubscribe()
    }
}
