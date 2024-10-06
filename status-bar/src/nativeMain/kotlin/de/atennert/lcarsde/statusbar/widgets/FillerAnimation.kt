package de.atennert.lcarsde.statusbar.widgets

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Controls the update/animation lifecycle for all filler widgets.
 */
@ExperimentalForeignApi
class FillerAnimation {

    private val fillerWidgets = mutableListOf<StatusFillerWidget>()

    private var updateJob: Job? = null

    private val minimalSwitchDelay = 2_000L // ms
    private val maximalSwitchDelay = 10_000L // ms

    fun addFillerWidget(fillerWidget: StatusFillerWidget) {
        fillerWidgets.add(fillerWidget)
    }

    fun clearWidgets() {
        fillerWidgets.clear()
    }

    private fun animateFiller() {
        val widget = fillerWidgets.randomOrNull() ?: return

        widget.update()
    }

    fun startAnimation() {
        fillerWidgets.forEach(StatusFillerWidget::start)
        updateJob = GlobalScope.launch(Dispatchers.Unconfined) {
            while (true) {
                delay(Random.nextLong(minimalSwitchDelay, maximalSwitchDelay))
                animateFiller()
            }
        }
    }

    fun stopAnimation() {
        updateJob?.cancel()
        updateJob = null
        fillerWidgets.forEach(StatusFillerWidget::stop)
    }
}