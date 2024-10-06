package de.atennert.lcarswm.lifecycle

/**
 * List of things that needs to be closed (executed for cleanup) when the
 * window manager shuts down. The things are ordered by the time of adding
 * them using closeWith. They are closed in reversed order.
 */
private val closables = mutableListOf<() -> Unit>()

/**
 * Close all registered closables in reversed order of registration. This
 * has to be called on shutdown of the window manager.
 */
fun closeClosables() {
    closables.asReversed()
        .forEach { closable ->
            try {
                closable()
            } catch (e: Throwable) {
                // don't do anything
            }
        }

    closables.clear()
}

/**
 * Register a class instance as closable by providing it's close function.
 * The function is used to clean up on window manager shutdown.
 */
fun <T> T.closeWith(closeFunction: T.() -> Unit): T {
    closables.add { this.closeFunction() }
    return this
}
