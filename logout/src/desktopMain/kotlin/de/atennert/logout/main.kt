package de.atennert.appSelector

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val queueName = "/lcarsde"
    val mq = MQ(queueName, MQ.Mode.READ_WRITE, true)

    Window(
        onCloseRequest = {
            mq.close()
            exitApplication()
        },
        title = "KotlinProject 1",
    ) {
        print("Window: ")
        println("0x" + this.window.windowHandle.toString(16))

        App(mq)
    }
}