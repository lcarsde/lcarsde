package de.atennert.lcarswm

import gtk.Display
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual data class Environment(
    val display: CPointer<Display>?
)