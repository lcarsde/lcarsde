package de.atennert.lcarsde.files

import kotlinx.cinterop.*
import platform.posix.*


@OptIn(ExperimentalForeignApi::class)
fun execute(vararg command: String) {
    val byteArgs = command.map { it.encodeToByteArray().pin() }
    val convertedArgs = nativeHeap.allocArrayOfPointersTo(byteArgs.map { it.addressOf(0).pointed })
    when (fork()) {
        -1 -> return
        0 -> {
            if (setsid() == -1) {
                perror("setsid failed")
                exit(1)
            }

            if (execvp(command[0], convertedArgs) == -1) {
                perror("execvp failed")
                exit(1)
            }

            exit(0)
        }
    }
    byteArgs.map { it.unpin() }
}

@ExperimentalForeignApi
fun execute(command: String) {
    val commandParts = command.split(' ')

    execute(*commandParts.toTypedArray())
}
