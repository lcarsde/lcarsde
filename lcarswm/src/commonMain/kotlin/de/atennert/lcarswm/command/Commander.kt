package de.atennert.lcarswm.command

abstract class Commander {
    fun run(command: String): Boolean {
        return run(command.split(' '))
    }

    abstract fun run(command: List<String>): Boolean
}