package de.atennert.lcarswm.screen

import de.atennert.lcarswm.ScreenMode
import de.atennert.lcarswm.system.FunctionCall
import de.atennert.rx.BehaviorSubject
import de.atennert.rx.operators.combineLatestWith
import de.atennert.rx.operators.map
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import xlib.RROutput
import kotlin.math.max

@ExperimentalForeignApi
open class ScreenManagerMock : ScreenManager<RROutput> {
    val functionCalls = mutableListOf<FunctionCall>()

    val lastScreenBuildersSj = BehaviorSubject(
        listOf(
            createScreenBuilder(
                1,
                isPrimary = true
            )
        )
    )
    private val lastScreenBuildersObs = lastScreenBuildersSj.asObservable()
    val lastScreenBuilders by lastScreenBuildersSj

    private val screenModeSj = BehaviorSubject(ScreenMode.NORMAL)
    override val screenModeObs = screenModeSj.asObservable()
    var screenMode by screenModeSj

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
        functionCalls.add(FunctionCall("updateScreenList"))
    }

    override fun toggleScreenMode() {
        functionCalls.add(FunctionCall("toggleScreenMode"))
    }

    override fun toggleFramedScreenMode() {
        functionCalls.add(FunctionCall("toggleFramedScreenMode"))
    }

    companion object {
        fun createScreenBuilder(
            id: Int,
            name: String = "",
            x: Int = 0,
            y: Int = 0,
            width: Int = 800,
            height: Int = 600,
            isPrimary: Boolean = false
        ): Screen.Builder<RROutput> {
            return Screen.Builder<RROutput>(id.convert(), name, isPrimary)
                .setMeasurements(x, y, width, height)
        }
    }
}