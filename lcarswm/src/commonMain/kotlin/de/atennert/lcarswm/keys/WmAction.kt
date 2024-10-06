package de.atennert.lcarswm.keys

/**
 * Window manager internal actions.
 */
enum class WmAction(val keys: Set<String>) {
    WINDOW_TOGGLE_FWD(setOf("window-toggle-forward")),
    WINDOW_TOGGLE_BWD(setOf("window-toggle-backward")),
    WINDOW_MOVE_NEXT(setOf("window-move-next", "window-move-up")),
    WINDOW_MOVE_PREVIOUS(setOf("window-move-previous", "window-move-down")),
    WINDOW_SPLIT_UP(setOf("window-split-up")),
    WINDOW_SPLIT_DOWN(setOf("window-split-down")),
    WINDOW_SPLIT_LEFT(setOf("window-split-left")),
    WINDOW_SPLIT_RIGHT(setOf("window-split-right")),
    WINDOW_COMBINE_UP(setOf("window-combine-up")),
    WINDOW_COMBINE_DOWN(setOf("window-combine-down")),
    WINDOW_COMBINE_LEFT(setOf("window-combine-left")),
    WINDOW_COMBINE_RIGHT(setOf("window-combine-right")),
    WINDOW_CLOSE(setOf("window-close")),
    SCREEN_MODE_TOGGLE(setOf("screen-mode-toggle")),
    WM_QUIT(setOf("lcarswm-quit"));

    companion object {
        fun getActionByKey(key: String): WmAction? {
            return values().find { it.keys.contains(key) }
        }
    }
}