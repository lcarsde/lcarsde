package de.atennert.rx

open class Subscriber<T>(private var observer: Observer<T>? = null) : Subscription(), Observer<T> {
    override fun next(value: T) {
        observer?.next(value)
    }

    override fun error(error: Throwable) {
        observer?.error(error)

        unsubscribe()
    }

    override fun complete() {
        observer?.complete()

        unsubscribe()
    }

    override fun unsubscribe() {
        observer = null
    }
}