package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.files.list
import de.atennert.lcarsde.files.read
import de.atennert.lcarsde.logout.LcarsColors
import de.atennert.rx.NextObserver
import de.atennert.rx.operators.filter
import de.atennert.rx.operators.first
import de.atennert.rx.operators.map
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.SIGTERM
import platform.posix.kill

class LogoutDefinition : LogoutOptionDefinition {
    override val label = "Logout"
    override val color = LcarsColors.C_F96
    override val isAvailable = true

    /**
     * Terminate lcarswm.kexe.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun call() {
        list("/proc")
            .filter { it.matches(Regex("^[0-9]+$")) }
            .filter { read("/proc/$it/comm")?.trim() == "lcarswm.kexe" }
            .map(String::toInt)
            .first()
            .subscribe(NextObserver {
                kill(it, SIGTERM)
            })
    }
}
