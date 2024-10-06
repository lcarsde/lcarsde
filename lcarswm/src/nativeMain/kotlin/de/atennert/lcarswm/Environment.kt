package de.atennert.lcarswm

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Display

@OptIn(ExperimentalForeignApi::class)
actual data class Environment(
    val display: CPointer<Display>?
)