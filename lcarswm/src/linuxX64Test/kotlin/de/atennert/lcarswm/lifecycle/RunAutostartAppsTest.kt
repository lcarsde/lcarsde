package de.atennert.lcarswm.lifecycle

import de.atennert.lcarsde.file.AccessMode
import de.atennert.lcarsde.file.File
import de.atennert.lcarsde.file.Files
import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.lifecycle.closeClosables
import de.atennert.lcarsde.log.PrintLogger
import de.atennert.lcarswm.HOME_CONFIG_DIR_PROPERTY
import de.atennert.lcarswm.drivenPorts.process.ProcessBuilderFactory
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.file.Directory
import de.atennert.lcarswm.file.FileFactory
import de.atennert.mock.process.MockProcessBuilder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains

class RunAutostartAppsTest {
    private val mockProcessBuilder = MockProcessBuilder

    private fun createFakeFileFactory(dirFilesMap: Map<String, Set<String>> = emptyMap()): FileFactory {
        return object : FileFactory {
            override fun getDirectory(path: String): Directory? {
                return dirFilesMap[path]
                    ?.let {
                        object : Directory {
                            override fun readFiles(): Set<String> = it
                            override fun close() {}
                        }
                    }
            }

            override fun getFile(path: String, accessMode: AccessMode): File {
                throw NotImplementedError()
            }
        }
    }

    private class FakeFiles(val files: List<String>, val lines: Map<String, List<String>>) : Files {
        override fun open(path: String, mode: AccessMode): File {
            throw NotImplementedError()
        }

        override fun exists(path: String): Boolean {
            return files.contains(path)
        }

        override fun readLines(path: String, consumer: (String) -> Unit) {
            lines[path]?.forEach(consumer)
        }
    }

    private class FakeEnvironment : Environment {
        override fun get(name: String): String? {
            return when (name) {
                HOME_CONFIG_DIR_PROPERTY -> "/home/me/config"
                else -> null
            }
        }

        override fun set(name: String, value: String): Boolean = false
    }

    @BeforeTest
    fun setup() {
        ServiceLocator.provide<ProcessBuilderFactory> { MockProcessBuilder.Factory }
    }

    @AfterTest
    fun teardown() {
        closeClosables()
        ServiceLocator.clear()
    }

    @Test
    fun `run apps from user desktop file`() {
        val lines = mapOf(
            "/home/me/config/autostart/runMe.desktop" to listOf("exec=myapp --arg1 -v 42")
        )

        val fakeFactory = createFakeFileFactory(
            mapOf(
                "/home/me/config/autostart" to setOf("runMe.desktop")
            )
        )

        runAutostartApps(FakeEnvironment(), fakeFactory, FakeFiles(listOf(), lines), PrintLogger())

        assertContains(mockProcessBuilder.calls, listOf("dex", "/home/me/config/autostart/runMe.desktop"))
    }

    @Test
    fun `run apps from global desktop file`() {
        val lines = mapOf(
            "/etc/xdg/autostart/runMe.desktop" to listOf("exec=myapp --arg1 -v 42")
        )

        val fakeFactory = createFakeFileFactory(
            mapOf(
                "/etc/xdg/autostart" to setOf("runMe.desktop")
            )
        )

        runAutostartApps(FakeEnvironment(), fakeFactory, FakeFiles(listOf(), lines), PrintLogger())

        assertContains(mockProcessBuilder.calls, listOf("dex", "/etc/xdg/autostart/runMe.desktop"))
    }
}