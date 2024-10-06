package de.atennert.lcarswm.environment

interface Environment {
    operator fun get(name: String): String?

    operator fun set(name: String, value: String): Boolean
}