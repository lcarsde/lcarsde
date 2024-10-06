package de.atennert.lcarswm.log

import de.atennert.lcarswm.file.AccessMode
import de.atennert.lcarswm.file.Directory
import de.atennert.lcarswm.file.File
import de.atennert.lcarswm.file.FileFactory
import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.time.Time
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileLoggerTest {
    private val filePath = "/this/is/my/logfile.log"

    class FakeTime : Time {
        override fun getTime(format: String): String = "0"
    }

    class FakeFile(val path: String, val accessMode: AccessMode) : File {
        var closed = false
        var text = ""

        override fun write(text: String) {
            this.text = text
        }

        override fun writeLine(text: String) {
            this.text = text + "\n"
        }

        override fun close() {
            closed = true
        }
    }

    class FakeFileFactory : FileFactory {
        val openedFiles = mutableListOf<FakeFile>()

        override fun getFile(path: String, accessMode: AccessMode): File {
            return FakeFile(path, accessMode)
                .also { openedFiles.add(it) }
        }

        override fun getDirectory(path: String): Directory? = null
    }

    @AfterTest
    fun teardown() {
        closeClosables()
    }

    @Test
    fun `open and close file logger`() {
        val fileFactory = FakeFileFactory()
        FileLogger(fileFactory, this.filePath, FakeTime())
        assertEquals(this.filePath, fileFactory.openedFiles[0].path, "handed in file path not used")
        assertEquals(AccessMode.WRITE, fileFactory.openedFiles[0].accessMode, "not using log file for write only")

        closeClosables()
        assertTrue(fileFactory.openedFiles[0].closed, "closed other file then opened")
    }

    @Test
    fun `log debug info to file`() {
        val text = "this is my text"

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logDebug(text)
        assertEquals("0 - DEBUG: $text\n", fileFactory.openedFiles[0].text, "the written text doesn't fit (maybe missing \\n?)")
    }

    @Test
    fun `log info to file`() {
        val text = "this is my text"

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logInfo(text)
        assertEquals("0 -  INFO: $text\n", fileFactory.openedFiles[0].text, "the written text doesn't fit (maybe missing \\n?)")
    }

    @Test
    fun `log warning to file`() {
        val text = "this is my warning"

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logWarning(text)
        assertEquals("0 -  WARN: $text\n", fileFactory.openedFiles[0].text, "the written text doesn't fit (maybe missing \\n?)")
    }

    @Test
    fun `log warning with throwable to file`() {
        val text = "this is my warning"
        val errorMessage = "some error message"
        val throwable = Throwable(errorMessage)

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logWarning(text, throwable)
        assertTrue(fileFactory.openedFiles[0].text.startsWith("0 -  WARN: $text: $errorMessage\n"),
            "the written text doesn't fit (maybe missing \\n?):\n${fileFactory.openedFiles[0].text}")
    }

    @Test
    fun `log error to file`() {
        val text = "this is my error"

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logError(text)
        assertEquals("0 - ERROR: $text\n", fileFactory.openedFiles[0].text, "the written text doesn't fit (maybe missing \\n?)")
    }

    @Test
    fun `log error with throwable to file`() {
        val text = "this is my error"
        val errorMessage = "some error message"
        val throwable = Throwable(errorMessage)

        val fileFactory = FakeFileFactory()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logError(text, throwable)
        assertTrue(fileFactory.openedFiles[0].text.startsWith("0 - ERROR: $text: $errorMessage\n"),
            "the written text doesn't fit (maybe missing \\n?):\n${fileFactory.openedFiles[0].text}")
    }
}