package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.files.execute
import de.atennert.lcarsde.logout.LcarsColors
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
        execute("xdg-screensaver", "lock")
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
