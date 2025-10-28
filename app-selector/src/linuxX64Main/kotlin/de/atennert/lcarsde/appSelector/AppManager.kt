@file:OptIn(ExperimentalForeignApi::class)

package de.atennert.lcarsde.appSelector

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import platform.posix.getenv

class AppManager {
    val appsByCategory: Map<String, List<AppDescriptor>>

    init {
        appsByCategory = PATHS.flatMap(::loadApplications)
            .map { AppDescriptor(it.first, it.second) }
            .filter { it.name != null && !it.noDisplay }
            .fold(mutableMapOf<String, MutableList<AppDescriptor>>()) { map, desc ->
                if (!map.containsKey(desc.category)) {
                    map[desc.category] = mutableListOf()
                }
                map[desc.category]?.add(desc)
                map
            }
    }

    private fun loadApplications(path: String): Sequence<Pair<String, List<String>>> {
        try {
            return SystemFileSystem.list(Path(path))
                .asSequence()
                .filter { SystemFileSystem.metadataOrNull(it)?.isRegularFile ?: false }
                .map { Pair(SystemFileSystem.resolve(it).toString(), readLines(it)) }
        } catch (_: FileNotFoundException) {
            println("File not found: $path")
            return emptySequence()
        }
    }

    companion object {
        private val PATHS = arrayOf(
            "/usr/share/applications",
            "/var/lib/flatpak/exports/share/applications",
            "/var/lib/snapd/desktop/applications",
            "${getenv("HOME")}/.local/share/applications"
        )

        fun readLines(path: Path): List<String> {
            val lines = mutableListOf<String>()
            SystemFileSystem.source(path).buffered().use { source ->
                while (true) {
                    val line = source.readLine() ?: break
                    lines.add(line)
                }
            }
            return lines
        }
    }
}