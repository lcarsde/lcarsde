package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.executeCommand
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.extensions.setStyling
import kotlinx.cinterop.*
import statusbar.*

@ExperimentalForeignApi
class ButtonWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, null) {

    private var ref: StableRef<ButtonWidget>? = null

    init {
        widget = gtk_button_new_with_label(properties["text"]?.uppercase())!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
        val label = gtk_container_get_children(widget.reinterpret())?.pointed?.data!!
        gtk_label_set_xalign(label.reinterpret(), 1f)
        gtk_label_set_yalign(label.reinterpret(), 1f)

        var color = properties["color"]
        if (!colors.contains(color)) {
            color = "99f"
        }
        widget.setStyling(cssProvider, "button--$color", "button--long")
    }

    override fun start() {
        ref = StableRef.create(this)
        gSignalConnect(widget, "clicked",
                staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer -> executeBtnCommand(p) },
                ref!!.asCPointer())
    }

    override fun stop() {
        ref!!.dispose()
    }

    override fun update() {
        // Nothing to do
    }

    companion object {
        private val colors = arrayOf("f90", "c9c", "99c", "c66", "99f", "f96", "c69")

        fun executeBtnCommand(ref: COpaquePointer) {
            val command = ref.asStableRef<ButtonWidget>().get().properties["command"] ?: return
            executeCommand(command)
        }
    }
}