package de.atennert.lcarswm.system.api

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * API that combines all other APIs for system access
 */
@ExperimentalForeignApi
interface SystemApi: RandrApi, DrawApi, InputApi, EventApi, WindowUtilApi, PosixApi, FontApi