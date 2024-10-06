package de.atennert.lcarswm.system.api

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import xlib.*

/**
 * Interface for accessing X RANDR functions
 */
@ExperimentalForeignApi
interface RandrApi {
    fun rQueryExtension(eventBase: CPointer<IntVar>, errorBase: CPointer<IntVar>): Int

    fun rSelectInput(window: Window, mask: Int)

    fun rGetScreenResources(window: Window): CPointer<XRRScreenResources>?

    fun rGetOutputPrimary(window: Window): RROutput

    fun rGetOutputInfo(resources: CPointer<XRRScreenResources>, output: RROutput): CPointer<XRROutputInfo>?

    fun rGetCrtcInfo(resources: CPointer<XRRScreenResources>, crtc: RRCrtc): CPointer<XRRCrtcInfo>?
}