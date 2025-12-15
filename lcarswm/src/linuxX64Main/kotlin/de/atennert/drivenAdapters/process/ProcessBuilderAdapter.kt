package de.atennert.drivenAdapters.process

import de.atennert.lcarswm.drivenPorts.process.ProcessBuilder
import de.atennert.lcarswm.drivenPorts.process.ProcessBuilderFactory

class ProcessBuilderAdapter : ProcessBuilder {
    private val processBuilder = de.atennert.lcarsde.process.ProcessBuilder()

    override fun command(vararg command: String): ProcessBuilder {
        processBuilder.command(*command)
        return this
    }

    override fun command(command: String): ProcessBuilder {
        processBuilder.command(command)
        return this
    }

    override fun directory(directory: String): ProcessBuilder {
        processBuilder.directory(directory)
        return this
    }

    override fun start() = ProcessAdapter(processBuilder.start())

    object Factory : ProcessBuilderFactory {
        override fun create() = ProcessBuilderAdapter()
    }
}