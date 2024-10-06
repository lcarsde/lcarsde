package de.atennert.lcarswm

import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.file.Files

interface ResourceGenerator {
    fun createEnvironment(): Environment

    fun createFiles(): Files

    fun createFileFactory(): FileFactory
}