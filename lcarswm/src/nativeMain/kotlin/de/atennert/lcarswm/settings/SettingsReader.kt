package de.atennert.lcarswm.settings

import de.atennert.lcarswm.SETTINGS_FILE
import de.atennert.lcarswm.conversion.toKString
import de.atennert.lcarswm.conversion.toUByteArray
import de.atennert.lcarswm.file.Files
import de.atennert.lcarswm.keys.KeyAction
import de.atennert.lcarswm.keys.KeyBinding
import de.atennert.lcarswm.keys.KeyExecution
import de.atennert.lcarswm.keys.WmAction
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.system.api.SystemApi
import kotlinx.cinterop.*
import xlib.XML_ELEMENT_NODE
import xlib._xmlNode
import xlib.xmlDoc
import xlib.xmlStrcmp
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.emptyMap
import kotlin.collections.emptySet
import kotlin.collections.forEach
import kotlin.collections.mapOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.set
import kotlin.collections.setOf
import kotlin.collections.toMutableMap

@ExperimentalForeignApi
class SettingsReader(
    private val logger: Logger,
    private val systemApi: SystemApi,
    files: Files,
    configPath: String
) {
    private val settingsFilePath = "$configPath$SETTINGS_FILE"
    private val defaultSettingsFilePath = "/etc$SETTINGS_FILE"

    var keyBindings: Set<KeyBinding> = emptySet()
    private set

    var generalSettings: Map<GeneralSetting, String> = emptyMap()
    private set

    private val generalSettingsDefault = mapOf(
        GeneralSetting.TITLE to "LCARS",
        GeneralSetting.FONT to "Ubuntu Condensed",
    )

    init {
        var usedSettings = settingsFilePath
        if (!files.exists(settingsFilePath)) {
            usedSettings = defaultSettingsFilePath
            logger.logDebug("SettingsReader::init::using system settings")
        } else {
            logger.logDebug("SettingsReader::init::using user settings")
        }

        loadSettings(usedSettings)
    }

    private fun loadSettings(settingsFilePath: String) {
        val document = systemApi.readXmlFile(settingsFilePath)
        if (document != null) {
            logger.logDebug("SettingsReader::loadSettings::loading settings from XML")
            if (!readSettingsFromDocument(document)) {
                logger.logInfo("SettingsReader::loadSettings::loading internal default settings")
                loadInternalDefaultSettings()
            }
            systemApi.freeXmlDoc(document)
        } else {
            logger.logInfo("SettingsReader::loadSettings::loading internal default settings")
            loadInternalDefaultSettings()
        }
    }

    private fun readSettingsFromDocument(document: CPointer<xmlDoc>): Boolean {
        val rootElement = systemApi.getXmlRootElement(document)?.pointed ?: return false

        if (rootElement.name.toKString() != "lcarswm") {
            logger.logWarning("SettingsReader::readSettingsFromDocument::didn't find root tag: ${rootElement.name.toKString()}")
            return false
        }

        var node = rootElement.children?.get(0)
        while (node != null) {
            val successful = when (node.name.toKString()) {
                "key-config" -> readKeyConfig(node)
                "general" -> readGeneralConfig(node)
                else -> true // ignore everything else
            }
            if (!successful) {
                return false
            }

            node = node.next?.pointed
        }

        if (keyBindings.isEmpty()) return false
        generalSettings = fillMissingRequiredSettings(generalSettings)

        return true
    }

    private fun readKeyConfig(node: _xmlNode): Boolean {
        var bindingNode = node.children?.get(0)
        val newKeyBindings = mutableSetOf<KeyBinding>()

        while (bindingNode != null) {
            if (bindingNode.type != XML_ELEMENT_NODE) {
                bindingNode = bindingNode.next?.pointed
                continue
            }

            val keyBinding = getBinding(bindingNode) ?: return false
            logger.logDebug("read-config: ${keyBinding.keys}->${keyBinding.command}")
            newKeyBindings.add(keyBinding)

            bindingNode = bindingNode.next?.pointed
        }
        keyBindings = newKeyBindings
        return true
    }

    private fun getBinding(bindingNode: _xmlNode): KeyBinding? {
        val keysNode = getNodeForName(bindingNode, "keys") ?: return null

        val execNode = getNodeForName(bindingNode, "exec")

        val keys = keysNode.children?.get(0)?.content.toKString()
        if (keys.isEmpty()) return null

        if (execNode != null) { // check if it's a command execution
            val exec = execNode.children?.get(0)?.content.toKString()
            if (exec.isEmpty()) return null

            return KeyExecution(keys, exec)
        } else { // check if it's a window manager action
            val actionNode = getNodeForName(bindingNode, "action") ?: return null
            val actionKey = actionNode.children?.get(0)?.content.toKString()
            val action = WmAction.getActionByKey(actionKey) ?: return null

            return KeyAction(keys, action)
        }
    }

    private fun getNodeForName(parentNode: _xmlNode, nodeName: String): _xmlNode? {
        val nodeNamePtr = nodeName.toUByteArray().toCValues()
        var targetNode = parentNode.children?.get(0)

        while (targetNode != null) {
            if (xmlStrcmp(targetNode.name, nodeNamePtr) == 0) {
                return targetNode
            }
            targetNode = targetNode.next?.pointed
        }
        return null
    }

    private fun readGeneralConfig(node: _xmlNode): Boolean {
        var generalNode = node.children?.get(0)
        var nodeName: String
        var setting: GeneralSetting?
        var textContent: String
        val generalSettingsXml = mutableMapOf<GeneralSetting, String>()

        while (generalNode != null) {
            if (generalNode.type != XML_ELEMENT_NODE) {
                generalNode = generalNode.next?.pointed
                continue
            }

            nodeName = generalNode.name.toKString()
            setting = GeneralSetting.getSettingByKey(nodeName)
            textContent = generalNode.children?.get(0)?.content.toKString()

            if (setting != null && textContent.isNotEmpty()) {
                generalSettingsXml[setting] = textContent
            } else {
                logger.logWarning("SettingsReader::readGeneralConfig::unknown setting $nodeName - $textContent")
            }

            generalNode = generalNode.next?.pointed
        }
        generalSettings = generalSettingsXml
        return true
    }

    private fun loadInternalDefaultSettings() {
        keyBindings = setOf(
            KeyExecution("XF86AudioMute", "amixer set Master toggle"),
            KeyExecution("XF86AudioRaiseVolume", "amixer set Master 3%+"),
            KeyExecution("XF86AudioLowerVolume", "amixer set Master 3%-"),
            KeyAction("Alt+Tab", WmAction.WINDOW_TOGGLE_FWD),
            KeyAction("Alt+Shift+Tab", WmAction.WINDOW_TOGGLE_BWD),
            KeyAction("Lin+Alt+Up", WmAction.WINDOW_MOVE_NEXT),
            KeyAction("Lin+Alt+Down", WmAction.WINDOW_MOVE_PREVIOUS),
            KeyAction("Lin+Ctrl+Up", WmAction.WINDOW_SPLIT_UP),
            KeyAction("Lin+Ctrl+Down", WmAction.WINDOW_SPLIT_DOWN),
            KeyAction("Lin+Ctrl+Left", WmAction.WINDOW_SPLIT_LEFT),
            KeyAction("Lin+Ctrl+Right", WmAction.WINDOW_SPLIT_RIGHT),
            KeyAction("Lin+Ctrl+Shift+Up", WmAction.WINDOW_COMBINE_UP),
            KeyAction("Lin+Ctrl+Shift+Down", WmAction.WINDOW_COMBINE_DOWN),
            KeyAction("Lin+Ctrl+Shift+Left", WmAction.WINDOW_COMBINE_LEFT),
            KeyAction("Lin+Ctrl+Shift+Right", WmAction.WINDOW_COMBINE_RIGHT),
            KeyAction("Alt+F4", WmAction.WINDOW_CLOSE),
            KeyAction("Lin+M", WmAction.SCREEN_MODE_TOGGLE),
            KeyAction("Lin+Q", WmAction.WM_QUIT),
        )

        generalSettings = generalSettingsDefault
    }

    private fun fillMissingRequiredSettings(settings: Map<GeneralSetting, String>): Map<GeneralSetting, String> {
        val resultSettings = settings.toMutableMap()

        generalSettingsDefault.forEach { (key, value) ->
            if (!resultSettings.containsKey(key)) {
                resultSettings[key] = value
            }
        }

        return resultSettings
    }
}