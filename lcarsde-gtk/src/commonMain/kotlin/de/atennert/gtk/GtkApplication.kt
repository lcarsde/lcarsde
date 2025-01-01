package de.atennert.gtk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class GtkApplication(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private var stop = false

    init {
        gtkInit()
    }

    fun main() = runBlocking {
        while(!stop) {
            while (gtkEventsPending() != 0) {
                gtkMainIteration()
            }
            delay(50)
        }
    }

    fun mainQuit() {
        stop = true
    }
}


fun gtkApplication(f: suspend GtkApplication.() -> Unit) = runBlocking {
    f(GtkApplication(coroutineContext))
}