package de.atennert.lcarswm.command

import de.atennert.lcarswm.log.Logger
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.exitProcess

@ExperimentalForeignApi
class PosixCommander(private val logger: Logger) : Commander() {
    override fun run(command: List<String>): Boolean {
        return when (fork()) {
            -1 -> false
            0 -> {
                if (setsid() == -1) {
                    exitProcess(1)
                }

                execute(command[0], command)
                exitProcess(1)
            }
            else -> true
        }
    }

    private fun execute(fileName: String, args: List<String>) {
        memScoped {
            execvp(fileName, allocArrayOf(args.map { it.cstr.ptr }.plus(NULL?.reinterpret()) ))
        }

        strerror(errno)?.toKString()?.let { reason ->
            logger.logError("PosixCommander::run::(child) execvp failed: $reason")
        }
    }
}