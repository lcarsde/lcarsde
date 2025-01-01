package de.atennert.lcarsde.logout.definition

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import de.atennert.lcarsde.logout.LcarsColors
import de.atennert.lcarsde.logout.dbus.GBusType
import de.atennert.lcarsde.logout.dbus.GDBusCallFlags
import de.atennert.lcarsde.logout.dbus.GDBusProxyFlags
import de.atennert.lcarsde.logout.dbus.JnaDBus

class DBusDefinition(
    override val label: String,
    override val color: LcarsColors,
    consoleKitMethod: String?,
    systemdMethod: String
) : LogoutOptionDefinition {
    private val handler = getHandler(consoleKitMethod, systemdMethod)
    override val isAvailable: Boolean
        get() = handler != null

    override fun call() {
        checkNotNull(handler)()
    }

    private companion object {
        val dbus = JnaDBus.INSTANCE

        fun getHandler(consoleKitMethod: String?, systemdMethod: String): (() -> Unit)? {
            if (isSystemdMethodAvailable(systemdMethod)) {
                return { runSystemdMethod(systemdMethod) }
            }
            if (consoleKitMethod != null && isConsoleKitMethodAvailable(consoleKitMethod)) {
                return { runConsoleKitMethod(consoleKitMethod) }
            }

            return null
        }

        fun isSystemdMethodAvailable(methodName: String): Boolean {
            val proxy = getProxy(
                "org.freedesktop.login1",
                "/org/freedesktop/login1",
                "org.freedesktop.login1.Manager"
            )

            try {
                val result = dbus.g_dbus_proxy_call_sync(
                    proxy,
                    "Can$methodName",
                    null,
                    GDBusCallFlags.NONE,
                    100,
                    null,
                    null
                )
                return result != null && dbus.g_variant_get_string(dbus.g_variant_get_child_value(result, 0), null) == "yes"
            } catch (ex: Exception) {
                return false
            }
        }

        fun runSystemdMethod(methodName: String) {
            val proxy = getProxy(
                "org.freedesktop.login1",
                "/org/freedesktop/login1",
                "org.freedesktop.login1.Manager"
            )

            try {
                val interactive = dbus.g_variant_new_boolean(true)
                val parameters = dbus.g_variant_new_tuple(PointerByReference(interactive), 1)
                dbus.g_dbus_proxy_call_sync(
                    proxy,
                    methodName,
                    parameters,
                    GDBusCallFlags.NONE,
                    100,
                    null,
                    null
                )
            } catch (ex: Exception) {
                println(ex)
            }
        }

        fun isConsoleKitMethodAvailable(methodName: String): Boolean {
            val proxy = getProxy(
                "org.freedesktop.ConsoleKit",
                "/org/freedesktop/ConsoleKit",
                "org.freedesktop.ConsoleKit.Manager"
            )

            try {
                val result = dbus.g_dbus_proxy_call_sync(
                    proxy,
                    "Can$methodName",
                    null,
                    GDBusCallFlags.NONE,
                    100,
                    null,
                    null
                )
                return result != null && dbus.g_variant_get_boolean(dbus.g_variant_get_child_value(result, 0))
            } catch (ex: Exception) {
                return false
            }
        }

        fun runConsoleKitMethod(methodName: String) {
            val proxy = getProxy(
                "org.freedesktop.login1",
                "/org/freedesktop/login1",
                "org.freedesktop.login1.Manager"
            )

            try {
                dbus.g_dbus_proxy_call_sync(
                    proxy,
                    methodName,
                    null,
                    GDBusCallFlags.NONE,
                    100,
                    null,
                    null
                )
            } catch (_: Exception) {
            }

        }

        fun getProxy(
            name: String,
            objectPath: String,
            interfaceName: String,
        ): Pointer {
            val bus = dbus.g_bus_get_sync(GBusType.G_BUS_TYPE_SYSTEM, null, null)
            return dbus.g_dbus_proxy_new_sync(
                bus,
                GDBusProxyFlags.NONE,
                null,
                name,
                objectPath,
                interfaceName,
                null,
                null
            )
        }
    }
}

