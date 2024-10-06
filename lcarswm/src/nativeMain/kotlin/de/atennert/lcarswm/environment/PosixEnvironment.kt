package de.atennert.lcarswm.environment

import de.atennert.lcarswm.X_TRUE
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.setenv

@ExperimentalForeignApi
class PosixEnvironment : Environment {
    override fun get(name: String): String? {
        return getenv(name)?.toKString()
    }

    override fun set(name: String, value: String): Boolean {
        return setenv(name, value, X_TRUE) == 0
    }
}