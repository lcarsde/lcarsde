package de.atennert.lcarsde.statusbar.configuration

import de.atennert.lcarsde.statusbar.extensions.toKString
import de.atennert.lcarsde.statusbar.extensions.toUByteArray
import kotlinx.cinterop.*
import platform.posix.F_OK
import platform.posix.access
import platform.posix.getenv
import statusbar.*

@ExperimentalForeignApi
private val userConfigPath = getenv("XDG_CONFIG_HOME")?.toKString()
private const val SETTINGS_FILE = "/lcarsde/status-config.xml"

@ExperimentalForeignApi
val settingsFilePath = when {
    doUserSettingsExist() -> "${userConfigPath}$SETTINGS_FILE"
    else -> "/etc$SETTINGS_FILE"
}

@ExperimentalForeignApi
private fun doUserSettingsExist(): Boolean {
    return access("${userConfigPath}$SETTINGS_FILE", F_OK) != -1
}

@ExperimentalForeignApi
fun readConfiguration(settingsFilePath: String): Set<WidgetConfiguration> {
    val document = xmlReadFile(settingsFilePath, null, 0)
            ?: return emptySet()

    val widgetConfigs = HashSet<WidgetConfiguration>()

    xmlDocGetRootElement(document)?.pointed?.let { root ->
        var node = root.children?.get(0)
        while (node != null) {
            if (node.name.toKString() == "widget") {
                readWidgetConfig(node)?.let(widgetConfigs::add)
            }
            node = node.next?.pointed
        }
    }
    xmlFreeDoc(document)

    return widgetConfigs
}

@ExperimentalForeignApi
private val namePtr = "name".toUByteArray().toCValues()
@ExperimentalForeignApi
private val xPtr = "x".toUByteArray().toCValues()
@ExperimentalForeignApi
private val yPtr = "y".toUByteArray().toCValues()
@ExperimentalForeignApi
private val widthPtr = "width".toUByteArray().toCValues()
@ExperimentalForeignApi
private val heightPtr = "height".toUByteArray().toCValues()

@ExperimentalForeignApi
private fun readWidgetConfig(node: _xmlNode): WidgetConfiguration? {
    val name = xmlGetProp(node.ptr, namePtr)?.toKString() ?: return null
    var x: Int? = null
    var y: Int? = null
    var width: Int? = null
    var height: Int? = null

    var properties = emptyMap<String, String>()

    var configNode = node.children?.get(0)
    while (configNode != null) {
        if (configNode.name.toKString() == "position") {
            x = xmlGetProp(configNode.ptr, xPtr)?.toKString()?.toInt()
            y = xmlGetProp(configNode.ptr, yPtr)?.toKString()?.toInt()
            width = xmlGetProp(configNode.ptr, widthPtr)?.toKString()?.toInt()
            height = xmlGetProp(configNode.ptr, heightPtr)?.toKString()?.toInt()
        } else if (configNode.name.toKString() == "properties") {
            properties = readProperties(configNode)
        }

        configNode = configNode.next?.pointed
    }
    if (x == null || y == null || width == null || height == null) {
        return null
    }
    return WidgetConfiguration(name, x, y, width, height)
            .withProperties(properties)
}

@ExperimentalForeignApi
private val keyPtr = "key".toUByteArray().toCValues()
@ExperimentalForeignApi
private val valuePtr = "value".toUByteArray().toCValues()

@ExperimentalForeignApi
private fun readProperties(propertiesNode: _xmlNode): Map<String, String> {
    val properties = HashMap<String, String>()

    var propertyNode = propertiesNode.children?.get(0)
    while (propertyNode != null) {
        val key = xmlGetProp(propertyNode.ptr, keyPtr)?.toKString()
        val value = xmlGetProp(propertyNode.ptr, valuePtr)?.toKString()

        if (key != null && value != null) {
            properties[key] = value
        }

        propertyNode = propertyNode.next?.pointed
    }
    return properties
}
