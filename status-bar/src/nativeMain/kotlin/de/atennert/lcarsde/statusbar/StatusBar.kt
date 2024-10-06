package de.atennert.lcarsde.statusbar

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.configuration.readConfiguration
import de.atennert.lcarsde.statusbar.configuration.settingsFilePath
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.extensions.setStyling
import de.atennert.lcarsde.statusbar.widgets.*
import kotlin.native.concurrent.ThreadLocal
import kotlinx.cinterop.*
import statusbar.*

@ExperimentalForeignApi
class StatusBar {
    val window: CPointer<GtkWidget>

    init {
        gtk_css_provider_load_from_path(cssProvider, STYLE_PATH, null)

        window = gtk_window_new(GtkWindowType.GTK_WINDOW_TOPLEVEL)!!
        window.setStyling(cssProvider, "window")

        gSignalConnect(window, "realize", staticCFunction { w: CPointer<GtkWidget>? -> create(w) })
        gSignalConnect(window, "configure-event",
            staticCFunction { w: CPointer<GtkWidget>?, e: CPointer<GdkEvent>? -> configure(w, e) })

        gtk_grid_set_column_spacing(grid.reinterpret(), GAP_SIZE.convert())
        gtk_grid_set_row_spacing(grid.reinterpret(), GAP_SIZE.convert())

        gtk_container_add(window.reinterpret(), grid)
    }

    @ThreadLocal
    companion object {
        private val configuration = readConfiguration(settingsFilePath)
        private val cssProvider = gtk_css_provider_get_default()!!
        private val grid = gtk_grid_new()!!
        private val widgetFactory = WidgetFactory(cssProvider)

        private var widgets = emptyList<StatusWidget>()
        private val fillerAnimation = FillerAnimation()

        private var currentWidth = 40 // px
        private var initialized = false

        /**
         * Called when the window is created.
         *
         * Set the atom that tells lcarswm that this is the status bar tool window.
         */
        private fun create(widget: CPointer<GtkWidget>?) {
            val gdkWindow = gtk_widget_get_window(widget)
            gdk_window_set_events(gdkWindow, GDK_STRUCTURE_MASK)
            gdk_x11_window_set_utf8_property(gdkWindow, LCARSDE_STATUS_BAR, LCARSDE_STATUS_BAR)
        }

        private fun configure(widget: CPointer<GtkWidget>?, event: CPointer<GdkEvent>?) {
            val configureData = event?.pointed?.configure
            if (configureData != null) {
                gtk_widget_set_size_request(widget, configureData.width, configureData.height)

                if (configureData.width != currentWidth) {
                    currentWidth = configureData.width

                    updateLayout()
                }
            }
        }

        private fun updateLayout() {
            val (horizontalCells, leftOverPixels) = getCellsAndOverflow()
            if (initialized) {
                stop()
                fillerAnimation.clearWidgets()

                for (i in 1..3) {
                    gtk_grid_remove_row(grid.reinterpret(), 0)
                }
            }
            initialized = true

            val leftMostX = fillConfiguredWidgets(horizontalCells)
            fillEmptySpace(leftMostX, leftOverPixels)

            gtk_widget_show_all(grid)
            start()
        }

        private fun getCellsAndOverflow(): Pair<Int, Int> {
            var horizontalCells = currentWidth / (CELL_SIZE + GAP_SIZE)
            var leftOverPixels = currentWidth % (CELL_SIZE + GAP_SIZE)
            if (leftOverPixels >= CELL_SIZE) {
                horizontalCells++
                leftOverPixels -= CELL_SIZE
            } else {
                leftOverPixels += GAP_SIZE
            }
            return Pair(horizontalCells, leftOverPixels)
        }

        private fun fillConfiguredWidgets(horizontalCells: Int): Int {
            widgets = configuration.mapNotNull(widgetFactory::createWidget)
            var leftMostX = horizontalCells

            for (widget in widgets) {
                if (widget.x + widget.width > horizontalCells ||
                        widget.y + widget.height > 3) {
                    continue
                }

                val gridX = horizontalCells - widget.x - widget.width
                val gridY = widget.y

                gtk_grid_attach(grid.reinterpret(), widget.widget, gridX, gridY, widget.width, widget.height)

                if (leftMostX > gridX) {
                    leftMostX = gridX
                }
            }
            return leftMostX
        }

        private fun fillEmptySpace(leftMostX: Int, leftOverPixels: Int) {
            var pixelsToAdd = leftOverPixels
            if (leftMostX % 2 != 0) {
                pixelsToAdd += CELL_SIZE + GAP_SIZE
            }

            val fillerCount = leftMostX / 2
            if (fillerCount == 0) {
                fillWithEmptySpace(pixelsToAdd)
            } else {
                fillWithFillerWidgets(fillerCount, pixelsToAdd)
            }
        }

        private fun fillWithEmptySpace(pixelsToAdd: Int) {
            if (pixelsToAdd <= 0)
                return

            val colIdx = if (pixelsToAdd == GAP_SIZE) -1 else 0

            val configuration = WidgetConfiguration("", 0, 0, 0, 1)
                    .withAddedPx(pixelsToAdd)
            val emptyWidget = EmptyWidget(configuration, cssProvider)

            gtk_grid_attach(grid.reinterpret(), emptyWidget.widget, colIdx, 0, 1, 1)
        }

        private fun fillWithFillerWidgets(fillerCount: Int, pixelsToAdd: Int) {
            val addedPixelsPerFiller = addedPixelsPerFiller(fillerCount, pixelsToAdd)

            for (col in 0 until fillerCount) {
                for (row in 0..2) {
                    val configuration = WidgetConfiguration("", 0, 0, 2, 1)
                            .withAddedPx(addedPixelsPerFiller[col])
                    val filler = StatusFillerWidget(configuration, cssProvider)
                    fillerAnimation.addFillerWidget(filler)
                    gtk_grid_attach(grid.reinterpret(), filler.widget, col * 2, row, 2, 1)
                }
            }
        }

        private fun addedPixelsPerFiller(fillerCount: Int, pixelsToAdd: Int): IntArray {
            val fullPixels = pixelsToAdd / fillerCount
            val restPixels = pixelsToAdd % fillerCount

            val pixelsPerFiller = IntArray(fillerCount) { fullPixels }
            for (i in 0 until restPixels) {
                pixelsPerFiller[i] += 1
            }
            return pixelsPerFiller
        }

        private fun start() {
            widgets.forEach(StatusWidget::start)
            fillerAnimation.startAnimation()
        }

        fun stop() {
            fillerAnimation.stopAnimation()
            widgets.forEach(StatusWidget::stop)
        }
    }
}