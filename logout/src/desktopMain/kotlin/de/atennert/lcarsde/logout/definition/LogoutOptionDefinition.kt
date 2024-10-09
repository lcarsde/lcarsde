package de.atennert.lcarsde.logout.definition

import androidx.compose.ui.graphics.Color

interface LogoutOptionDefinition {
    val label: String
    val color: Color
    val isAvailable: Boolean

    fun call()
}
