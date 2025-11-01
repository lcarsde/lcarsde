package de.atennert.rx

fun interface Subscribable<T> {
    fun subscribe(observer: Observer<T>): Subscription
}