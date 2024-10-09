package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.LcarsColors
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class LockScreenDefinition : LogoutOptionDefinition {
    override val label = "Lock Screen"
    override val color = LcarsColors.C_99C
    override val isAvailable: Boolean
        get() = isLockScreenAvailable()

    override fun call() {
        val builder = ProcessBuilder()
        builder.command("xdg-screensaver", "lock")
            .start()
    }

    private fun isLockScreenAvailable(): Boolean {
        return System.getenv("PATH")
            .split(File.pathSeparator)
            .any { path ->
                Files.isExecutable(Paths.get(path, "xdg-screensaver"))
            }
    }
}
