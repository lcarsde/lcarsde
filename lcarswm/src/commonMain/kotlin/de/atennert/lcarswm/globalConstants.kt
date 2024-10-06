package de.atennert.lcarswm

import de.atennert.lcarswm.drawing.Color

const val HOME_CONFIG_DIR_PROPERTY = "XDG_CONFIG_HOME"

const val HOME_CACHE_DIR_PROPERTY = "XDG_CACHE_HOME"

const val LCARS_DE_DIR = "lcarsde"

const val LOG_FILE_PATH = "/$LCARS_DE_DIR/lcarswm.log"

const val SETTINGS_FILE = "/$LCARS_DE_DIR/settings.xml"

// layout values

const val BAR_HEIGHT = 40
const val BAR_GAP_SIZE = 8
const val BAR_HEIGHT_SMALL = (BAR_HEIGHT - BAR_GAP_SIZE) / 2
const val BAR_END_WIDTH = BAR_HEIGHT - BAR_GAP_SIZE
const val OUTER_CORNER_RADIUS_BIG = BAR_HEIGHT
const val OUTER_CORNER_RADIUS_SMALL = BAR_HEIGHT_SMALL
const val INNER_CORNER_RADIUS = BAR_HEIGHT_SMALL
const val DATA_AREA_HEIGHT = 3 * 40 + 2 * 8
const val DATA_BAR_HEIGHT = DATA_AREA_HEIGHT - INNER_CORNER_RADIUS
const val SIDE_BAR_WIDTH = 184
const val TITLE_BAR_OFFSET = 1 // extra pixel for overflowing fonts
const val BAR_HEIGHT_WITH_OFFSET = BAR_HEIGHT + TITLE_BAR_OFFSET // accommodate the font layout with a required extra pixel

const val NORMAL_WINDOW_LEFT_OFFSET = SIDE_BAR_WIDTH + BAR_GAP_SIZE + BAR_END_WIDTH + BAR_GAP_SIZE
const val NORMAL_WINDOW_UPPER_OFFSET = BAR_HEIGHT * 2 + INNER_CORNER_RADIUS + BAR_GAP_SIZE * 3 + DATA_BAR_HEIGHT
const val NORMAL_WINDOW_NON_APP_HEIGHT = NORMAL_WINDOW_UPPER_OFFSET + BAR_GAP_SIZE + INNER_CORNER_RADIUS * 2 + BAR_HEIGHT

const val LOWER_CORNER_WIDTH = OUTER_CORNER_RADIUS_BIG + 272

const val WINDOW_TITLE_FONT_SIZE = BAR_HEIGHT
const val WINDOW_TITLE_OFFSET = 30

// Base colors
val BLACK = Color("#000000")
val YELLOW = Color("#FF9900")
val ORCHID = Color("#CC99CC")
val DAMPENED_PURPLE = Color("#9999CC")
val DARK_RED = Color("#CC6666")
val SAND = Color("#FFCC99")
val BRIGHT_PURPLE = Color("#9999FF")
val ORANGE = Color("#FF9966")
val DARK_PINK = Color("#CC6699")

data class ColorSet(val base: Color, val light: Color, val dark: Color)
val COLOR_1 = ColorSet(Color("#AA7FAA"), Color("#BE9BB4"), Color("#906193"))
val COLOR_2 = ColorSet(Color("#6B477A"), Color("#9C7299"), Color("#533668"))
val COLOR_3 = ColorSet(Color("#5D3449"), Color("#915E75"), Color("#3D232E"))
val COLOR_4 = ColorSet(Color("#8E4465"), Color("#AB6D86"), Color("#6B2D47"))
val COLOR_5 = ColorSet(Color("#B5517F"), Color("#CA7896"), Color("#A73769"))
val COLOR_6 = ColorSet(Color("#C1574C"), Color("#D88274"), Color("#A9372E"))
val COLOR_7 = ColorSet(Color("#E6661D"), Color("#EE8F49"), Color("#F5571D"))
val COLOR_8 = ColorSet(Color("#ED924E"), Color("#EEAC7B"), Color("#E3722A"))
val COLOR_9 = ColorSet(Color("#D88568"), Color("#E39C84"), Color("#BE6044"))
val COLOR_10 = ColorSet(Color("#E26A49"), Color("#F09173"), Color("#E53B28"))

val COLOR_LOGO = YELLOW
val COLOR_ACTIVE_TITLE = YELLOW
val COLOR_INACTIVE_TITLE = COLOR_6.base
val COLOR_BAR_ENDS = COLOR_1.base
val COLOR_BACKGROUND = BLACK

val COLOR_NORMAL_BAR_DOWN = COLOR_8.base
val COLOR_NORMAL_SIDEBAR_DOWN = COLOR_6.light
val COLOR_NORMAL_BAR_MIDDLE_1 = COLOR_1.light
val COLOR_NORMAL_BAR_MIDDLE_2 = COLOR_5.light
val COLOR_NORMAL_BAR_MIDDLE_3 = COLOR_8.base
val COLOR_NORMAL_BAR_MIDDLE_4 = COLOR_9.base
val COLOR_NORMAL_CORNER_1 = COLOR_1.base
val COLOR_NORMAL_CORNER_2 = COLOR_8.base
val COLOR_NORMAL_CORNER_3 = COLOR_9.light

val COLOR_MAX_BAR_UP = COLOR_5.light
val COLOR_MAX_BAR_DOWN = COLOR_5.light
