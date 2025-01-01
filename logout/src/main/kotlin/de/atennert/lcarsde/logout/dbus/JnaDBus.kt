package de.atennert.lcarsde.logout.dbus

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

object GBusType {
    const val G_BUS_TYPE_SYSTEM: Int = 1
}

object GDBusProxyFlags {
    const val NONE: Int = 0
}

object GDBusCallFlags {
    const val NONE: Int = 0
}

interface JnaDBus : Library {
    fun g_bus_get_sync(
        bus_type: Int, // GBusType
        cancellable: PointerByReference?, // GCancellable
        error: PointerByReference? // GError
    ): Pointer // GDBusConnection

    fun g_dbus_proxy_new_sync(
        connection: Pointer, // GDBusConnection
        flags: Int, // GDBusProxyFlags
        info: PointerByReference?, // GDBusInterfaceInfo
        name: String,
        object_path: String,
        interface_name: String,
        cancellable: PointerByReference?, // GCancellable
        error: PointerByReference? // GError
    ): Pointer // GDBusProxy

    fun g_dbus_proxy_call_sync(
        proxy: Pointer, // GDBusProxy
        method_name: String,
        parameters: Pointer?, // GVariant
        flags: Int, // GDBusCallFlags
        timeout_msec: Int,
        cancellable: PointerByReference?, // GCancellable
        error: PointerByReference? // GError
    ): Pointer? // GVariant

    fun g_variant_new_boolean(
        value: Boolean
    ): Pointer // GVariant

    fun g_variant_new_tuple(
        children: PointerByReference,
        n_children: Int
    ): Pointer // GVariant

    fun g_variant_get_string(value: Pointer, length: PointerByReference?): String

    fun g_variant_get_boolean(value: Pointer): Boolean

    fun g_variant_get_child_value(value: Pointer, index: Int): Pointer

    companion object {
        val INSTANCE: JnaDBus = Native.load("gio-2.0", JnaDBus::class.java)
    }
}