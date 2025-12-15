package de.atennert.mock.process

import de.atennert.lcarswm.drivenPorts.process.Process
import de.atennert.lcarswm.drivenPorts.process.ProcessBuilder
import de.atennert.lcarswm.drivenPorts.process.ProcessBuilderFactory

object MockProcessBuilder : ProcessBuilder {
    val calls = mutableListOf<List<String>>()

    override fun command(vararg command: String): ProcessBuilder {
        calls.add(command.toList())
        return this
    }

    override fun command(command: String): ProcessBuilder {
        val commandParts = command.split(' ')
        return command(*commandParts.toTypedArray())
    }

    override fun directory(directory: String): ProcessBuilder {
        TODO("Not yet implemented")
    }

    override fun start() = object : Process {}

    fun clear() {
        calls.clear()
    }

    object Factory : ProcessBuilderFactory {
        override fun create() = MockProcessBuilder
    }
}