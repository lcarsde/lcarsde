package de.atennert.lcarsde.menu.gtk

enum class GtkPolicyType(val value: Int) {
    ALWAYS(0),
    AUTOMATIC(1),
    NEVER(2),
    EXTERNAL(3)
}