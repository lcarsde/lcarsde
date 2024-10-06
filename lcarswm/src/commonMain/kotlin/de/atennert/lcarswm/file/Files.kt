package de.atennert.lcarswm.file

interface Files {
    fun exists(path: String): Boolean

    fun readLines(path: String, consumer: (String) -> Unit)
}