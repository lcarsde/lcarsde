package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.logout.LcarsdeColors

interface LogoutOptionDefinition {
    val label: String
    val color: LcarsdeColors
    val isAvailable: Boolean

    fun call()
}
