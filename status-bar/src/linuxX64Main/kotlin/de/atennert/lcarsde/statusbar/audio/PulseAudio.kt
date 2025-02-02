package de.atennert.lcarsde.statusbar.audio

import kotlinx.cinterop.*
import platform.posix.SIGINT
import statusbar.*

typealias VolumeCallback = (volume: Float, muted: Boolean) -> Unit

@OptIn(ExperimentalForeignApi::class)
class Volume(paVolume: pa_cvolume) {
    private var values = readPA(paVolume)

    fun useAsPaVolume(f: (pa_cvolume) -> Unit) {
        val paVolume = nativeHeap.alloc<pa_cvolume>()

        paVolume.channels = values.size.toUByte()
        values.forEachIndexed { index, value ->
            paVolume.values[index] = value
        }
        f(paVolume)
        values = readPA(paVolume)

        nativeHeap.free(paVolume)
    }

    private fun readPA(paVolume: pa_cvolume) =
        (0 until paVolume.channels.toInt()).map { paVolume.values[it] }
}

/**
 * https://gist.github.com/jasonwhite/1df6ee4b5039358701d2
 */
@OptIn(ExperimentalForeignApi::class)
object PulseAudio {
    private var mainLoop: CPointer<*>? = null
    private var mainLoopApi: CPointer<pa_mainloop_api>? = null
    private var context: CPointer<*>? = null
    private var signal: CPointer<*>? = null
    private var callback: VolumeCallback = { _, _ -> }

    private var currentDefaultSinkName: String? = null
    private var currentVolume: Volume? = null

    fun start(callback: VolumeCallback) {
        this.callback = callback
        mainLoop = pa_mainloop_new()
        mainLoopApi = mainLoop?.let { pa_mainloop_get_api(it.reinterpret()) }
        context = mainLoopApi?.let { pa_context_new(it, "lcarsde status bar") }

        if (context == null) {
            return
        }
        if (pa_signal_init(mainLoopApi) != 0) {
            return
        }
        signal = pa_signal_new(
            SIGINT,
            staticCFunction { _, _, _, ref -> exitSignalCallback(ref) },
            StableRef.create(this).asCPointer()
        )

        val connectResult = pa_context_connect(context?.reinterpret(), null, PA_CONTEXT_NOAUTOSPAWN.convert(), null)
        if (connectResult < 0) {
            return
        }
        pa_context_set_state_callback(
            context?.reinterpret(),
            staticCFunction { context, ref -> stateCallback(context, ref) },
            StableRef.create(this).asCPointer(),
        )
    }

    fun run() {
        mainLoop?.apply { pa_mainloop_iterate(this.reinterpret(), 0, cValuesOf(1)) }
    }

    fun stop() {
        callback = { _, _ -> }
        currentDefaultSinkName = null
        currentVolume = null

        mainLoopApi?.pointed?.quit?.invoke(mainLoopApi, 0)

        signal?.apply {
            pa_signal_free(this.reinterpret())
            pa_signal_done()
        }
        signal = null

        context?.apply { pa_context_unref(this.reinterpret()) }
        context = null

        mainLoop?.apply { pa_mainloop_free(this.reinterpret()) }
        mainLoop = null
        mainLoopApi = null
    }

    private fun exitSignalCallback(ref: COpaquePointer?) {
        val pulseAudio = ref?.asStableRef<PulseAudio>()?.get()
        pulseAudio?.stop()
    }

    private fun stateCallback(context: CPointer<*>?, ref: COpaquePointer?) {
        val pulseAudio = ref?.asStableRef<PulseAudio>()?.get()

        when (pa_context_get_state(context?.reinterpret())) {
            pa_context_state.PA_CONTEXT_READY -> {
                println("Connected to PulseAudio")
                pa_context_get_server_info(
                    context?.reinterpret(),
                    staticCFunction { ctx, info, r -> serverInfoCallback(ctx, info, r) },
                    ref
                )

                pa_context_set_subscribe_callback(
                    context?.reinterpret(),
                    staticCFunction { ctx, type, idx, r -> subscribeCallback(ctx, type, idx, r) },
                    ref
                )
                pa_context_subscribe(context?.reinterpret(), PA_SUBSCRIPTION_MASK_SINK.convert(), null, null)
            }

            pa_context_state.PA_CONTEXT_TERMINATED -> {
                println("PulseAudio connection terminated")
                pulseAudio?.stop()
            }

            pa_context_state.PA_CONTEXT_UNCONNECTED -> {
                println("PulseAudio disconnected")
                pulseAudio?.stop()
            }

            pa_context_state.PA_CONTEXT_FAILED -> {
                println("PulseAudio connection failed: ${pa_strerror(pa_context_errno(context?.reinterpret()))}")
                pulseAudio?.stop()
            }

            pa_context_state.PA_CONTEXT_SETTING_NAME,
            pa_context_state.PA_CONTEXT_AUTHORIZING,
            pa_context_state.PA_CONTEXT_CONNECTING -> {
                /* Nothing to do */
            }
        }
    }

    private fun serverInfoCallback(context: CPointer<*>?, info: CPointer<pa_server_info>?, ref: COpaquePointer?) {
        val sinkName = info?.pointed?.default_sink_name?.toKString()
        currentDefaultSinkName = sinkName
        println("default audio sink name: $sinkName")
        pa_context_get_sink_info_by_name(
            context?.reinterpret(),
            sinkName,
            staticCFunction { _, sinkInfo, _, r -> sinkInfoCallback(sinkInfo, r) },
            ref
        )
    }

    private fun subscribeCallback(context: CPointer<*>?, type: pa_subscription_event_type, idx: UInt, ref: COpaquePointer?) {
        val facility = type.and(PA_SUBSCRIPTION_EVENT_FACILITY_MASK.toUInt())

        if (facility == PA_SUBSCRIPTION_EVENT_SINK.toUInt()) {
            val operation = pa_context_get_sink_info_by_index(
                context?.reinterpret(),
                idx,
                staticCFunction { _, sinkInfo, _, r -> sinkInfoCallback(sinkInfo, r) },
                ref
            )
            operation?.apply { pa_operation_unref(this) }
        }
    }

    private fun sinkInfoCallback(info: CPointer<pa_sink_info>?, ref: COpaquePointer?) {
        if (info == null) {
            return
        }

        currentVolume = Volume(info.pointed.volume)
        println("${info.pointed.volume.channels}, ${info.pointed.volume.values[0]}, ${info.pointed.volume.values[1]}")
        val pulseAudio = ref?.asStableRef<PulseAudio>()?.get()
        val volume = (pa_cvolume_avg(info.pointed.volume.ptr).toFloat() / PA_VOLUME_NORM.toFloat()) * 100
        val muted = info.pointed.mute > 0

        pulseAudio?.callback?.invoke(volume, muted)
    }

    fun setMute(mute: Boolean) {
        if (context == null) {
            return
        }
        currentDefaultSinkName?.apply {
            pa_context_set_sink_mute_by_name(
                context?.reinterpret(),
                this,
                if (mute) 1 else 0,
                staticCFunction { _, _, _ -> },
                null
            )
        }
    }

    fun raiseVolume(increase: Int) {
        if (context == null) {
            return
        }
        val volumeStep = (PA_VOLUME_NORM - PA_VOLUME_MUTED).toFloat() * (increase / 100f)

        currentVolume?.let { volume -> currentDefaultSinkName?.let { name -> volume to name } }
            ?.also { (volume, name) ->
                volume.useAsPaVolume {
                    pa_cvolume_inc(it.ptr, volumeStep.toUInt())
                    pa_context_set_sink_volume_by_name(
                        context?.reinterpret(),
                        name,
                        it.ptr,
                        staticCFunction { _, _, _ -> },
                        null
                    )
                }
            }
    }

    fun lowerVolume(decrease: Int) {
        if (context == null) {
            return
        }
        val volumeStep = (PA_VOLUME_NORM - PA_VOLUME_MUTED).toFloat() * (decrease / 100f)

        currentVolume?.let { volume -> currentDefaultSinkName?.let { name -> volume to name } }
            ?.also { (volume, name) ->
                volume.useAsPaVolume {
                    pa_cvolume_dec(it.ptr, volumeStep.toUInt())
                    pa_context_set_sink_volume_by_name(
                        context?.reinterpret(),
                        name,
                        it.ptr,
                        staticCFunction { _, _, _ -> },
                        null
                    )
                }
            }
    }
}