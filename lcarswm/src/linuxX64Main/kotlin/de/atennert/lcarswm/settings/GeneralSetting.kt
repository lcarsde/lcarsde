package de.atennert.lcarswm.settings

enum class GeneralSetting(val key: String) {
    TITLE("title"),
    TITLE_IMAGE("title-image"),
    FONT("font");

    companion object {
        fun getSettingByKey(key: String): GeneralSetting? {
            return entries.find { it.key == key }
        }
    }
}