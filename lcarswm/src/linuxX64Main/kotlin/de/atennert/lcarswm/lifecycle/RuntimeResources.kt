package de.atennert.lcarswm.lifecycle

import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

@ExperimentalForeignApi
data class RuntimeResources(val xEvent: XEventResources, val appMenu: AppMenuResources, val platform: PlatformResources<RROutput>)