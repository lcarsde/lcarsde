package de.atennert.rx

open class Subscription(private val fUnsubscribe: () -> Unit = {}) {
    private var isUnsubscribed = false

    private val subscriptions = mutableSetOf<Subscription>()
    fun add(subscription: Subscription) {
        if (isUnsubscribed) {
            subscription.unsubscribe()
        } else {
            subscriptions.add(subscription)
        }
    }

    open fun unsubscribe() {
        fUnsubscribe()

        isUnsubscribed = true
        subscriptions.forEach { it.unsubscribe() }
        subscriptions.clear()
    }
}