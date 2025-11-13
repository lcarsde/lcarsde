package de.atennert.lcarswm

import de.atennert.lcarsde.file.Files
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.FileFactory

interface ResourceGenerator {
    fun createEnvironment(): Environment

    fun createFiles(): Files

    fun createFileFactory(): FileFactory
}