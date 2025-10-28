package de.atennert.lcarsde.appSelector

import de.atennert.lcarsde.process.ProcessBuilder
import gtk.getenv
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlin.system.exitProcess

class AppDescriptor(private val filePath: String, data: List<String>) {
    val name = data.find { it.startsWith("Name=") }?.drop(5)
    val category = data.find { it.startsWith("Categories=") }?.drop(11)?.split(';').let(::getPreferredCategory)
    val noDisplay = data.find { it.startsWith("NoDisplay=") }?.drop(10)?.equals("true", true) ?: false
    val color = BUTTON_COLORS[name?.sumOf(Char::code)?.mod(BUTTON_COLORS.size) ?: 0]

    @OptIn(ExperimentalForeignApi::class)
    fun start() {
        val builder = ProcessBuilder()
        builder.command("dex", filePath)
            .directory(getenv("HOME")?.toKString() ?: throw IllegalStateException("HOME not set"))
            .start()
        exitProcess(0)
    }

    private companion object {
        val PREFERRED_CATEGORIES = arrayOf(
            "System", "Game", "Network", "Office", "Settings", "AudioVideo", "Development", "Graphics", "Utility"
        )

        val BUTTON_COLORS = arrayOf(
            LcarsColors.C_C9C,
            LcarsColors.C_99C,
            LcarsColors.C_F96
        )

        fun getPreferredCategory(categories: List<String>?): String {
            if (categories.isNullOrEmpty()) {
                return "Utility"
            }
            for (preferredCategory in PREFERRED_CATEGORIES) {
                if (categories.contains(preferredCategory)) {
                    return preferredCategory
                }
            }
            return categories[0]
        }
    }
}