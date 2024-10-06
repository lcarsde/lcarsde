package de.atennert.lcarswm.mouse

import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.monitor.Monitor
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.window.ManagedWmWindow
import de.atennert.lcarswm.window.WindowCoordinator
import de.atennert.rx.NextObserver
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.RROutput
import xlib.Window

@OptIn(ExperimentalForeignApi::class)
class MoveWindowManager(
    private val logger: Logger,
    private val windowCoordinator: WindowCoordinator,
    monitorManager: MonitorManager<RROutput>
) {

    private var monitors = emptyList<Monitor<RROutput>>()

    private lateinit var lastWindowMonitor: Monitor<RROutput>

    private var targetWindow: ManagedWmWindow<Window>? = null

    init {
        monitorManager.monitorsObs.subscribe(NextObserver { monitors ->
            // TODO use more RX stuff
            this.monitors = monitors
            logger.logDebug("MoveWindowManager::updateMonitors::$monitors")

            // cancel moving the window
            targetWindow?.let {
                logger.logWarning("MoveWindowManager::updateMonitors::cancel moving of ${it.id}")
            }
            targetWindow = null
        })
            .closeWith { this.unsubscribe() }
    }

    fun press(window: ManagedWmWindow<Window>, x: Int, y: Int) {
        targetWindow?.let {
            logger.logWarning("MoveWindowManager::press::apparently there was a window moving in process for ${it.id}.")
        }

        targetWindow = window
        lastWindowMonitor = getMonitor(x, y) ?: return
    }

    fun move(x: Int, y: Int) {
        targetWindow?.let {
            val newMonitor = getMonitor(x, y) ?: return
            if (newMonitor.id != lastWindowMonitor.id) {
                windowCoordinator.moveWindowToMonitor(it.id, newMonitor)
                lastWindowMonitor = newMonitor
            }
        }
    }

    fun release() {
        targetWindow = null
    }

    private fun getMonitor(x: Int, y: Int): Monitor<RROutput>? {
        return try {
            monitors.first { it.isOnMonitor(x, y) }
        } catch (e: Exception) {
            logger.logError(e.toString())
            null
        }
    }
}