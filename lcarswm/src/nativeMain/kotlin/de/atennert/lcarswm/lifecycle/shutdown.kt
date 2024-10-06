package de.atennert.lcarswm.lifecycle

import de.atennert.lcarswm.system.api.SystemApi
import kotlinx.cinterop.ExperimentalForeignApi
import staticLogger


/**
 * Full shutdown routines.
 */
@ExperimentalForeignApi
fun shutdown(system: SystemApi) {
    system.sync(false)

    staticLogger = null

    closeClosables()
}
