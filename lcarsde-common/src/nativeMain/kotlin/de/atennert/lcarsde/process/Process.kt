package de.atennert.lcarsde.process

import de.atennert.lcarsde.lifecycle.inject
import de.atennert.lcarsde.log.Logger
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.exitProcess

class Process(val directory: String?, val command: List<String>) {
    private val pid: Int
    private var isRunning = false
    private val logger by inject<Logger>()

    init {
        if (command.isEmpty()) {
            throw IllegalArgumentException("Process::init::No command given")
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
            logger.logError("Process::changeDirectory::Failed to change directory to $directory")
            exitProcess(1)
        }
    }

    private fun createSession() {
        if (setsid() == -1) {
            logger.logError("Process::createSession::setsid failed")
            exitProcess(2)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun runCommand() {
        memScoped {
            // execvp needs NUL-terminated strings in a NULL-terminated array
            val args = allocArrayOf(command.map { it.cstr.ptr } + null)

            if (execvp(command[0], args) == -1) {
                strerror(errno)?.toKString()?.let { reason ->
                    logger.logError("Process::runCommand::execvp failed: $reason")
                }
                exitProcess(1)
            }
        }
    }
}