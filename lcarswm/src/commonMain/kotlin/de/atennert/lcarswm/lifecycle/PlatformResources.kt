package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.command.Commander
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.file.Files
import de.atennert.lcarswm.monitor.MonitorManager

data class PlatformResources<Output>(
    val commander: Commander,
    val fileFactory: FileFactory,
    val files: Files,
    val monitorManager: MonitorManager<Output>,
    val environment: Environment
)
