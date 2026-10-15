package de.atennert.lcarswm.lifecycle

import de.atennert.lcarsde.file.Files
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.screen.ScreenManager

data class PlatformResources<Output>(
    val fileFactory: FileFactory,
    val files: Files,
    val screenManager: ScreenManager<Output>,
    val environment: Environment
)