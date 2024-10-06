package de.atennert.lcarsde.statusbar

import kotlinx.cinterop.*
import statusbar.*

@ExperimentalForeignApi
fun executeCommand(command: String) {
    val commandParts = command.split(' ')

    val byteArgs = commandParts.map { it.encodeToByteArray().pin() }
    val convertedArgs = nativeHeap.allocArrayOfPointersTo(byteArgs.map { it.addressOf(0).pointed })
    when (fork()) {
        -1 -> return
        0 -> {
            if (setsid() == -1) {
                perror("setsid failed")
                exit(1)
            }

            if (execvp(commandParts[0], convertedArgs) == -1) {
                perror("execvp failed")
                exit(1)
            }

            exit(0)
        }
    }
    byteArgs.map { it.unpin() }
}
