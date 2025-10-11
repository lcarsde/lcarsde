package de.atennert.lcarsde.process

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.exitProcess

class Process(val directory: String?, val command: List<String>) {
    private val pid: Int
    private var isRunning = false

    init {
        if (command.isEmpty()) {
            throw IllegalArgumentException("No command given")
        }

        pid = start()
        if (pid > 0) {
            isRunning = true
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun start(): Int {
        when (val forkResult = fork()) {
            0 -> {
                changeDirectory()
                createSession()
                runCommand()

                exitProcess(0)
            }
            else -> return forkResult
        }
    }

    private fun changeDirectory() {
        if (directory == null) {
            return
        }
        if (chdir(directory) != 0) {
            perror("Failed to change directory to $directory")
            exitProcess(1)
        }
    }

    private fun createSession() {
        if (setsid() == -1) {
            println("setsid failed")
            exitProcess(2)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun runCommand() {
        val byteArgs = command.map { it.encodeToByteArray().pin() }
        val convertedArgs = nativeHeap.allocArrayOfPointersTo(byteArgs.map { it.addressOf(0).pointed })

        if (execvp(command[0], convertedArgs) == -1) {
            println("execvp failed")
            byteArgs.map { it.unpin() }
            exitProcess(1)
        }
        byteArgs.map { it.unpin() }
    }
}