package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.logout.LcarsColors
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.jvm.optionals.getOrNull

class LogoutDefinition : LogoutOptionDefinition {
    override val label = "Logout"
    override val color = LcarsColors.C_F96
    override val isAvailable = true

    /**
     * Terminate lcarswm.kexe.
     */
    override fun call() {
        val wmPID = Files.list(Paths.get("/proc"))
            .filter { it.name.matches(Regex("^[0-9]+$"))}
            .filter { Files.readString(it.resolve("comm")).trim() == "lcarswm.kexe" }
            .map { it.name.toLong() }
            .findFirst()
            .getOrNull()

        if (wmPID != null) {
            ProcessHandle.of(wmPID).ifPresent(ProcessHandle::destroy)
        }
    }
}
