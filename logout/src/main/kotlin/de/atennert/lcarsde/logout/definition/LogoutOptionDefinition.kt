package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.logout.LcarsColors

interface LogoutOptionDefinition {
    val label: String
    val color: LcarsColors
    val isAvailable: Boolean

    fun call()
}
