package de.atennert.lcarsde.file

interface File {
    fun write(text: String)

    fun writeLine(text: String)

    fun close()
}