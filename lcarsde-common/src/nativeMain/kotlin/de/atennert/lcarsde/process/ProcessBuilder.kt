package de.atennert.lcarsde.process

class ProcessBuilder {
    private var command: List<String> = emptyList()
    private var directory: String? = null

    fun command(vararg command: String): ProcessBuilder {
        this.command = command.toList()
        return this
    }

    fun command(command: String): ProcessBuilder {
        val commandParts = command.split(' ')

        return command(*commandParts.toTypedArray())
    }

    fun directory(directory: String): ProcessBuilder {
        return this
    }

    fun start(): Process {
        return Process(directory, command)
    }
}