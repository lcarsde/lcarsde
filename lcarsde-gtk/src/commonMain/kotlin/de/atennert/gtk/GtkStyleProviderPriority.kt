package de.atennert.gtk

enum class GtkStyleProviderPriority(val value: Int) {
    FALLBACK(1),
    USER(800),
}