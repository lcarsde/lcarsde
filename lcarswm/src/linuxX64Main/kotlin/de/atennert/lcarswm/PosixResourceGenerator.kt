package de.atennert.lcarswm

import de.atennert.lcarsde.file.Files
import de.atennert.lcarsde.file.PosixFiles
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.environment.PosixEnvironment
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.file.PosixFileFactory
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
class PosixResourceGenerator : ResourceGenerator {
    override fun createEnvironment(): Environment = PosixEnvironment()

    override fun createFiles(): Files = PosixFiles

    override fun createFileFactory(): FileFactory = PosixFileFactory()
}