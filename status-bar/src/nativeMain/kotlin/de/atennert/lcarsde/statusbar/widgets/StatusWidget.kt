package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.CELL_SIZE
import de.atennert.lcarsde.statusbar.GAP_SIZE
import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import statusbar.GtkCssProvider
import statusbar.GtkWidget

/**
 * Base class for all status widgets.
 *
 * @param widgetConfiguration The configuration for the widget
 * @param cssProvider The CSS provider for styling all parts of the widget
 * @param updateDelayMs The update delay for the update job. If this is null, no update job is started.
 */
@ExperimentalForeignApi
abstract class StatusWidget(private val widgetConfiguration: WidgetConfiguration,
                            protected val cssProvider: CPointer<GtkCssProvider>,
                            private val updateDelayMs: Long?) {

    val x: Int get() = widgetConfiguration.x
    val y: Int get() = widgetConfiguration.y
    val width: Int get() = widgetConfiguration.width
    val height: Int get() = widgetConfiguration.height
    val widthPx: Int get() = CELL_SIZE * width + GAP_SIZE * (width - 1) + widgetConfiguration.addedPx
    val heightPx: Int get() = CELL_SIZE * height + GAP_SIZE * (height - 1)
    val properties: Map<String, String> get() = widgetConfiguration.properties

    private var updateJob: Job? = null

    lateinit var widget: CPointer<GtkWidget>
        protected set

    /**
     * Start update job of the widget.
     */
    open fun start() {
        // no delay -> don't start the job
        if (updateDelayMs == null)
            return

        updateJob = GlobalScope.launch(Dispatchers.Unconfined) {
            while (true) {
                update()
                delay(updateDelayMs)
            }
        }
    }

    /**
     * Stop the update job.
     */
    open fun stop() {
        updateJob?.cancel()
        updateJob = null
    }

    /**
     * Called for triggering an update of the widget.
     */
    abstract fun update()
}