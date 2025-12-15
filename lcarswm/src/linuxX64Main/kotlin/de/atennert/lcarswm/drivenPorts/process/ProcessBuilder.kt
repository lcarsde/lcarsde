package de.atennert.lcarswm.drivenPorts.process

interface ProcessBuilderFactory {
    fun create(): ProcessBuilder
}

interface ProcessBuilder {
    fun command(vararg command: String): ProcessBuilder

    fun command(command: String): ProcessBuilder

    fun directory(directory: String): ProcessBuilder

    fun start(): Process
}

interface Process