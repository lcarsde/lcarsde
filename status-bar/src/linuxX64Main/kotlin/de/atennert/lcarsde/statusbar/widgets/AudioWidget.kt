package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.CELL_SIZE
import de.atennert.lcarsde.statusbar.GAP_SIZE
import de.atennert.lcarsde.statusbar.audio.PulseAudio
import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import gtk.*
import kotlinx.cinterop.*

@ExperimentalForeignApi
class AudioWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>) :
    StatusWidget(widgetConfiguration, cssProvider, 300) {

    private var ref: StableRef<AudioWidget>? = null

    private val volumeDrawArea: CPointer<GtkWidget>
    private val lowerButton: Pair<CPointer<GtkWidget>, CPointer<GtkWidget>>
    private val muteButton: Pair<CPointer<GtkWidget>, CPointer<GtkWidget>>
    private val raiseButton: Pair<CPointer<GtkWidget>, CPointer<GtkWidget>>

    private var currentAudioStatus = Pair(false, 0)

    init {
        widget = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, GAP_SIZE)!!
        lowerButton = createButton("button--left", "button--f90")
        muteButton = createButton("button--middle", "button--99f")
        volumeDrawArea = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(volumeDrawArea, CELL_SIZE, CELL_SIZE)
        raiseButton = createButton("button--right", "button--f90")

        gSignalConnect(lowerButton.second, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t> -> drawLower(c) })
        gSignalConnect(muteButton.second, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t> -> drawMute(c) })
        gSignalConnect(raiseButton.second, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t> -> drawRaise(c) })

        gtk_box_pack_start(widget.reinterpret(), lowerButton.first, FALSE, FALSE, 0.convert())
        gtk_box_pack_start(widget.reinterpret(), muteButton.first, FALSE, FALSE, 0.convert())
        gtk_box_pack_start(widget.reinterpret(), volumeDrawArea, FALSE, FALSE, 0.convert())
        gtk_box_pack_start(widget.reinterpret(), raiseButton.first, FALSE, FALSE, 0.convert())

        gtk_widget_set_size_request(widget, widthPx, heightPx)
    }


    private fun createButton(vararg styleClasses: String): Pair<CPointer<GtkWidget>, CPointer<GtkWidget>> {
        val button = gtk_button_new()!!
        gtk_widget_set_size_request(button, CELL_SIZE, CELL_SIZE)

        val styleContext = gtk_widget_get_style_context(button)
        for (cls in styleClasses) {
            gtk_style_context_add_class(styleContext, cls)
        }
        gtk_style_context_add_provider(styleContext, cssProvider.reinterpret(), GTK_STYLE_PROVIDER_PRIORITY_USER.convert())

        val iconArea = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(iconArea, CELL_SIZE, CELL_SIZE)
        gtk_container_add(button.reinterpret(), iconArea)
        return Pair(button, iconArea)
    }

    override fun start() {
        PulseAudio.start { volume, muted ->
            println("volume = $volume, muted = $muted")
            currentAudioStatus = Pair(muted, volume.toInt())
            gtk_widget_queue_draw(volumeDrawArea)
            setMuteStatus()
        }
        ref = StableRef.create(this)


        gSignalConnect(
            lowerButton.first, "clicked",
            staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer -> lowerVolume(p) },
            ref!!.asCPointer()
        )
        gSignalConnect(
            muteButton.first, "clicked",
            staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer -> toggleMute(p) },
            ref!!.asCPointer()
        )
        gSignalConnect(
            raiseButton.first, "clicked",
            staticCFunction { _: CPointer<GtkWidget>, p: COpaquePointer -> raiseVolume(p) },
            ref!!.asCPointer()
        )

        gSignalConnect(
            volumeDrawArea, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t>, p: COpaquePointer -> drawVolume(c, p) },
            ref!!.asCPointer()
        )

        super.start()
    }

    override fun stop() {
        super.stop()
        PulseAudio.stop()

        ref!!.dispose()
    }

    override fun update() {
        PulseAudio.run()
    }

    private fun setMuteStatus() {
        val styleContext = gtk_widget_get_style_context(muteButton.first)
        if (currentAudioStatus.first) {
            gtk_style_context_add_class(styleContext, "button--c66")
            gtk_style_context_remove_class(styleContext, "button--99f")
        } else {
            gtk_style_context_add_class(styleContext, "button--99f")
            gtk_style_context_remove_class(styleContext, "button--c66")
        }
    }

    companion object {
        private fun drawLower(context: CPointer<cairo_t>) {
            cairo_set_source_rgb(context, 0.0, 0.0, 0.0)
            drawSpeaker(context)

            cairo_move_to(context, 14.0, 20.0)
            cairo_line_to(context, 20.0, 20.0)
            cairo_stroke(context)
        }

        private fun drawMute(context: CPointer<cairo_t>) {
            cairo_set_source_rgb(context, 0.0, 0.0, 0.0)
            drawSpeaker(context)

            cairo_move_to(context, 10.0, 28.0)
            cairo_line_to(context, 30.0, 14.0)
            cairo_stroke(context)
        }

        private fun drawRaise(context: CPointer<cairo_t>) {
            cairo_set_source_rgb(context, 0.0, 0.0, 0.0)
            drawSpeaker(context)

            cairo_move_to(context, 14.0, 20.0)
            cairo_line_to(context, 20.0, 20.0)
            cairo_move_to(context, 17.0, 17.0)
            cairo_line_to(context, 17.0, 23.0)
            cairo_stroke(context)
        }

        private fun drawSpeaker(context: CPointer<cairo_t>) {
            cairo_move_to(context, 28.0, 10.0)
            cairo_line_to(context, 28.0, 30.0)
            cairo_line_to(context, 20.0, 25.0)
            cairo_line_to(context, 12.0, 25.0)
            cairo_line_to(context, 12.0, 15.0)
            cairo_line_to(context, 20.0, 15.0)
            cairo_close_path(context)
            cairo_stroke(context)
        }

        private fun drawVolume(context: CPointer<cairo_t>, ref: COpaquePointer) {
            val widget = ref.asStableRef<AudioWidget>().get()
            val (mute, volume) = widget.currentAudioStatus

            if (mute || volume == 0) {
                cairo_set_source_rgb(context, 0.6, 0.6, 0.8)
            } else {
                cairo_set_source_rgb(context, 1.0, 0.8, 0.6)
            }

            // draw sound triangle border
            cairo_move_to(context, 0.0, 39.0)
            cairo_line_to(context, 39.0, 39.0)
            cairo_line_to(context, 39.0, 0.0)
            cairo_close_path(context)
            cairo_stroke(context)

            // draw volume level
            val displayVolume = volume * 40.0 / 100
            cairo_rectangle(context, 0.0, 0.0, displayVolume, 39.0)
            cairo_fill(context)

            // clear volume level drawn above triangle
            cairo_set_source_rgb(context, 0.0, 0.0, 0.0)
            cairo_move_to(context, 0.0, 0.0)
            cairo_line_to(context, 38.0, 0.0)
            cairo_line_to(context, 0.0, 38.0)
            cairo_close_path(context)
            cairo_fill(context)
        }

        private fun lowerVolume(ref: COpaquePointer) {
            val widget = ref.asStableRef<AudioWidget>().get()
            println(widget.properties)
            widget.properties["volumeDecrease"]?.let {
                PulseAudio.lowerVolume(it.trimEnd('%').toInt())
            }
        }

        private fun raiseVolume(ref: COpaquePointer) {
            val widget = ref.asStableRef<AudioWidget>().get()
            widget.properties["volumeIncrease"]?.let {
                PulseAudio.raiseVolume(it.trimEnd('%').toInt())
            }
        }

        private fun toggleMute(ref: COpaquePointer) {
            val widget = ref.asStableRef<AudioWidget>().get()
            PulseAudio.setMute(!widget.currentAudioStatus.first)
        }
    }
}