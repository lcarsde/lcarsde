package de.atennert.lcarswm.lifecycle

import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput

@ExperimentalForeignApi
data class RuntimeResources(val xEvent: XEventResources, val sideBar: SideBarResources, val platform: PlatformResources<RROutput>)