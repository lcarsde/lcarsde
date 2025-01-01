package de.atennert.gtk

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import de.atennert.Uint32_t

interface GTK: Library {
    fun gtk_init(argc: IntByReference, argv: Pointer?)

    fun gtk_widget_show_all(widget: Pointer)

    fun gtk_css_provider_load_from_path(
        css_provider: Pointer,
        path: String,
        error: Pointer?,
    )

    fun gtk_window_new(type: Uint32_t): Pointer

    fun gtk_scrolled_window_new(hadjustment: Pointer?, vadjustment: Pointer?): Pointer

    fun gtk_scrolled_window_set_policy(
        scrolled_window: Pointer,
        hscrollbar_policy: Uint32_t,
        vscrollbar_policy: Uint32_t,
    )

    fun gtk_box_new(orientation: Uint32_t, spacing: Int): Pointer

    fun gtk_box_pack_end(box: Pointer, child: Pointer, expand: Boolean, fill: Boolean, padding: Uint32_t)

    fun gtk_box_pack_start(box: Pointer, child: Pointer, expand: Boolean, fill: Boolean, padding: Uint32_t)

    fun gtk_css_provider_new(): Pointer

    fun gtk_widget_get_style_context(widget: Pointer): Pointer

    fun gtk_style_context_add_class(context: Pointer, class_name: String)

    fun gtk_style_context_remove_class(context: Pointer, class_name: String)

    fun gtk_style_context_has_class(context: Pointer, class_name: String): Boolean

    fun gtk_style_context_add_provider(
        context: Pointer,
        provider: Pointer,
        priority: Uint32_t
    )

    fun gtk_container_add(container: Pointer, widget: Pointer)

    fun gtk_container_remove(container: Pointer, widget: Pointer)

    fun gtk_widget_get_window(widget: Pointer): Pointer

    fun gtk_events_pending(): Int

    fun gtk_main_iteration(): Int

    fun gtk_label_new(str: String): Pointer

    fun gtk_button_new(): Pointer

    fun gtk_button_set_label(button: Pointer, label: String)

    fun gtk_button_set_alignment(button: Pointer, xalign: Float, yalign: Float)

    fun gtk_widget_set_size_request(widget: Pointer, width: Int, height: Int)

    companion object {
        val INSTANCE: GTK = Native.load("gtk-3", GTK::class.java)
    }
}

interface GDK : Library {
    fun gdk_x11_window_set_utf8_property(
        window: Pointer,
        name: String,
        value: String
    )

    companion object {
        val INSTANCE: GDK = Native.load("gdk-3", GDK::class.java)
    }
}

fun interface SignalCallback : Callback {
    fun callbackMethod()
}

interface GObject : Library {
    fun g_signal_connect_data(
        instance: Pointer,
        detailed_signal: String,
        c_handler: SignalCallback,
        data: Pointer?,
        destroy_data: Pointer?,
        connect_flags: Uint32_t
    )

    companion object {
        val INSTANCE: GObject = Native.load("gobject-2.0", GObject::class.java)
    }
}

fun interface IdleCallback : Callback {
    fun onIdle(data: Pointer?): Boolean
}

interface GLib : Library {
    fun g_idle_add(function: IdleCallback, data: Pointer?)

    companion object {
        val INSTANCE: GLib = Native.load("glib-2.0", GLib::class.java)
    }
}