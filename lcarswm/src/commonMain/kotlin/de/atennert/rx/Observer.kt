package de.atennert.rx

interface Observer<T> {
    fun next(value: T)

    fun error(error: Throwable)

    fun complete()
}