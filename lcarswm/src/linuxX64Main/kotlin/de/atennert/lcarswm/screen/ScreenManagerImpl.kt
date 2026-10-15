package de.atennert.lcarswm.screen

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.api.RandrApi
import de.atennert.rx.BehaviorSubject
import de.atennert.rx.operators.combineLatestWith
import de.atennert.rx.operators.map
import gtk.Window
import kotlinx.cinterop.*
import xlib.RRCrtc
import xlib.RROutput
import xlib.XRROutputInfo
import xlib.XRRScreenResources
import kotlin.math.max

@ExperimentalForeignApi
class ScreenManagerImpl(private val randrApi: RandrApi, private val rootWindowId: Window) : ScreenManager<RROutput> {

    private val lastScreenBuildersSj = BehaviorSubject<List<Screen.Builder<RROutput>>>(emptyList())
    private val lastScreenBuildersObs = lastScreenBuildersSj.asObservable()

    private val screenModeSj = BehaviorSubject(ScreenMode.NORMAL)
    override val screenModeObs = screenModeSj.asObservable()
    private var screenMode by screenModeSj

    override val screensObs = lastScreenBuildersObs
        .combineLatestWith(screenModeObs)
        .map { (screenBuilders, screenMode) ->
            screenBuilders.map { it.setScreenMode(screenMode).build() }
        }

    override val primaryScreenObs = screensObs
        .map { screens -> screens.firstOrNull { it.isPrimary } }

    override val combinedScreenSizeObs = screensObs
        .map { screens ->
            screens.fold(Pair(0, 0)) { (oldWidth, oldHeight), screen ->
                Pair(
                    max(screen.x + screen.width, oldWidth),
                    max(screen.y + screen.height, oldHeight)
                )
            }
        }

    override fun updateScreenList() {
        val screenData = getScreenData()
        val screenIds = getScreenIds(screenData)
        val primary = getPrimary(screenIds)

        val activeScreenInfos = screenIds
            .map { screenId -> Pair(screenId, randrApi.rGetOutputInfo(screenData, screenId)) }
            .filter { (_, outputInfo) -> outputInfo != null && outputInfo.pointed.crtc.convert<Int>() != 0 }

        val screenNames = activeScreenInfos
            .map { (_, outputInfo) -> getOutputName(outputInfo!!) }

        lastScreenBuildersSj.next(
            activeScreenInfos
                .map { (screenId, _) -> screenId }
                .zip(screenNames)
                .map { (id, name) -> Screen.Builder(id, name, id == primary) }
                .zip(activeScreenInfos.map { it.second })
                .map { (screen, outputInfo) ->
                    addMeasurementToScreen(
                        screen,
                        outputInfo!!.pointed.crtc,
                        screenData
                    )
                }
                .sortedBy { (it.y + it.height).toULong().shl(32) + it.x.toULong() })
    }

    private fun getScreenData(): CPointer<XRRScreenResources> {
        return randrApi.rGetScreenResources(rootWindowId)!!
    }

    private fun getScreenIds(screenData: CPointer<XRRScreenResources>): Array<RROutput> {
        val screenResources = screenData.pointed
        return Array(screenResources.noutput) { screenResources.outputs!![it] }
    }

    private fun getPrimary(screenIds: Array<RROutput>): RROutput {
        val primaryScreenId = randrApi.rGetOutputPrimary(rootWindowId)

        return if (screenIds.contains(primaryScreenId)) {
            primaryScreenId
        } else {
            screenIds[0]
        }
    }

    /**
     * Get the name of the given output.
     */
    private fun getOutputName(outputObject: CPointer<XRROutputInfo>): String {
        val name = outputObject.pointed.name
        val nameArray = ByteArray(outputObject.pointed.nameLen) { name!![it] }

        return nameArray.decodeToString()
    }

    private fun addMeasurementToScreen(
        screen: Screen.Builder<RROutput>,
        crtcReference: RRCrtc,
        screenData: CPointer<XRRScreenResources>
    ): Screen.Builder<RROutput> {
        val crtcInfo = randrApi.rGetCrtcInfo(screenData, crtcReference)!!.pointed

        screen.setMeasurements(crtcInfo.x, crtcInfo.y, crtcInfo.width.convert(), crtcInfo.height.convert())

        return screen
    }

    override fun toggleScreenMode() {
        screenMode = when (screenMode) {
            ScreenMode.NORMAL -> ScreenMode.MAXIMIZED
            ScreenMode.MAXIMIZED -> ScreenMode.FULLSCREEN
            ScreenMode.FULLSCREEN -> ScreenMode.NORMAL
        }
    }

    override fun toggleFramedScreenMode() {
        screenMode = if (screenMode == ScreenMode.NORMAL) {
            ScreenMode.MAXIMIZED
        } else {
            ScreenMode.NORMAL
        }
    }
}