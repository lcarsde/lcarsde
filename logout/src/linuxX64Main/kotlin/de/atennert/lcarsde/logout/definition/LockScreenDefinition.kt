package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.logout.LcarsColors
import de.atennert.lcarsde.process.ProcessBuilder
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.X_OK
import platform.posix.access
import platform.posix.getenv

class LockScreenDefinition : LogoutOptionDefinition {
    override val label = "Lock Screen"
    override val color = LcarsColors.C_99C
    override val isAvailable: Boolean
        get() = isLockScreenAvailable()

    override fun call() {
        ProcessBuilder()
            .command("xdg-screensaver", "lock")
            .start()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun isLockScreenAvailable(): Boolean {
        return getenv("PATH")
            ?.toKString()
            ?.split(":")
            ?.any { path ->
                access("$path/xdg-screensaver", X_OK) == 0
            } ?: false
    }
}
