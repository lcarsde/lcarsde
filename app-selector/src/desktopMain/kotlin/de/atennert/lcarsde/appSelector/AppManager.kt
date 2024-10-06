package de.atennert.lcarsde.appSelector

import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence

class AppManager {
    val appsByCategory: Map<String, List<AppDescriptor>>

    init {
        appsByCategory = PATHS.flatMap(::loadApplications)
            .asSequence()
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
            return Files.list(Paths.get(path))
                .map(Path::toFile)
                .filter(File::isFile)
                .map { Pair(it.absolutePath, it.readLines()) }
                .asSequence()
        } catch (e: NoSuchFileException) {
            println("Directory not found: $path")
            return emptySequence()
        }
    }

    companion object {
        private val PATHS = arrayOf(
            "/usr/share/applications",
            "/var/lib/flatpak/exports/share/applications",
            "${System.getenv("HOME")}/.local/share/applications"
        )
    }
}