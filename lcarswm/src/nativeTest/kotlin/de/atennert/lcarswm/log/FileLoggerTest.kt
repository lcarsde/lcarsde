package de.atennert.lcarswm.log

import de.atennert.lcarsde.file.AccessMode
import de.atennert.lcarsde.file.File
import de.atennert.lcarsde.file.Files
import de.atennert.lcarsde.lifecycle.closeClosables
import de.atennert.lcarsde.log.FileLogger
import de.atennert.lcarsde.time.Time
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

    class FakeFiles : Files {
        val openedFiles = mutableListOf<FakeFile>()

        override fun open(path: String, mode: AccessMode): File {
            return FakeFile(path, mode)
                .also { openedFiles.add(it) }
        }

        override fun exists(path: String): Boolean {
            throw NotImplementedError()
        }

        override fun readLines(path: String, consumer: (String) -> Unit) {
            throw NotImplementedError()
        }
    }

    @AfterTest
    fun teardown() {
        closeClosables()
    }

    @Test
    fun `open and close file logger`() {
        val fileFactory = FakeFiles()
        FileLogger(fileFactory, this.filePath, FakeTime())
        assertEquals(this.filePath, fileFactory.openedFiles[0].path, "handed in file path not used")
        assertEquals(AccessMode.WRITE, fileFactory.openedFiles[0].accessMode, "not using log file for write only")

        closeClosables()
        assertTrue(fileFactory.openedFiles[0].closed, "closed other file then opened")
    }

    @Test
    fun `log debug info to file`() {
        val text = "this is my text"

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logDebug(text)
        assertEquals(
            "0 - DEBUG: $text\n",
            fileFactory.openedFiles[0].text,
            "the written text doesn't fit (maybe missing \\n?)"
        )
    }

    @Test
    fun `log info to file`() {
        val text = "this is my text"

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logInfo(text)
        assertEquals(
            "0 -  INFO: $text\n",
            fileFactory.openedFiles[0].text,
            "the written text doesn't fit (maybe missing \\n?)"
        )
    }

    @Test
    fun `log warning to file`() {
        val text = "this is my warning"

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logWarning(text)
        assertEquals(
            "0 -  WARN: $text\n",
            fileFactory.openedFiles[0].text,
            "the written text doesn't fit (maybe missing \\n?)"
        )
    }

    @Test
    fun `log warning with throwable to file`() {
        val text = "this is my warning"
        val errorMessage = "some error message"
        val throwable = Throwable(errorMessage)

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logWarning(text, throwable)
        assertTrue(
            fileFactory.openedFiles[0].text.startsWith("0 -  WARN: $text: $errorMessage\n"),
            "the written text doesn't fit (maybe missing \\n?):\n${fileFactory.openedFiles[0].text}"
        )
    }

    @Test
    fun `log error to file`() {
        val text = "this is my error"

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logError(text)
        assertEquals(
            "0 - ERROR: $text\n",
            fileFactory.openedFiles[0].text,
            "the written text doesn't fit (maybe missing \\n?)"
        )
    }

    @Test
    fun `log error with throwable to file`() {
        val text = "this is my error"
        val errorMessage = "some error message"
        val throwable = Throwable(errorMessage)

        val fileFactory = FakeFiles()
        val fileLogger = FileLogger(fileFactory, this.filePath, FakeTime())

        fileLogger.logError(text, throwable)
        assertTrue(
            fileFactory.openedFiles[0].text.startsWith("0 - ERROR: $text: $errorMessage\n"),
            "the written text doesn't fit (maybe missing \\n?):\n${fileFactory.openedFiles[0].text}"
        )
    }
}