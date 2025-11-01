package de.atennert.rx

interface Observer<in T> {
    fun next(value: T)

    fun error(error: Throwable)

    fun complete()
}