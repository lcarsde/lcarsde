package de.atennert.rx

import kotlin.test.Test
import kotlin.test.assertTrue

class SubscriptionTest {
    @Test
    fun `unsubscribe with registered function`() {
        var iWasCalled = false
        val subscription = Subscription { iWasCalled = true }

        subscription.unsubscribe()

        assertTrue(iWasCalled)
    }

    @Test
    fun `unsubscribe with registered subscriptions`() {
        var iWasCalled = false
        val innerSubscription = Subscription { iWasCalled = true }
        val subscription = Subscription()
        subscription.add(innerSubscription)

        subscription.unsubscribe()

        assertTrue(iWasCalled)
    }

    @Test
    fun `unsubscribed subscription should unsubscribe new added subscriptions immediately`() {
        val subscription = Subscription()
        subscription.unsubscribe()

        var iWasCalled = false
        val innerSubscription = Subscription { iWasCalled = true }
        subscription.add(innerSubscription)

        assertTrue(iWasCalled)
    }
}