package de.atennert.lcarsde.file

interface Files {
    fun open(path: String, mode: AccessMode): File

    fun exists(path: String): Boolean

    fun readLines(path: String, consumer: (String) -> Unit)
}