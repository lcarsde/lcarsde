package de.atennert.rx

class NextObserver<T>(private val nextHandler: NextHandler<T>): Observer<T> {
    override fun next(value: T) {
        nextHandler.handle(value)
    }

    override fun error(error: Throwable) {}

    override fun complete() {}

    fun interface NextHandler<T> {
        fun handle(value: T)
    }
}