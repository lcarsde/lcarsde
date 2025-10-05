package de.atennert.lcarsde.logout.definition

import de.atennert.lcarsde.logout.LcarsColors
import gtk.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
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

    @ExperimentalForeignApi
    private companion object {
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
                val result = g_dbus_proxy_call_sync(
                    proxy,
                    "Can$methodName",
                    null,
                    G_DBUS_CALL_FLAGS_NONE,
                    100,
                    null,
                    null
                )
                return result != null && g_variant_get_string(g_variant_get_child_value(result, 0u), null)?.toKString() == "yes"
            } catch (_: Exception) {
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
                val interactive = g_variant_new_boolean(1)
                val parameters = g_variant_new_tuple(cValuesOf(interactive), 1u)
                g_dbus_proxy_call_sync(
                    proxy,
                    methodName,
                    parameters,
                    G_DBUS_CALL_FLAGS_NONE,
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
                val result = g_dbus_proxy_call_sync(
                    proxy,
                    "Can$methodName",
                    null,
                    G_DBUS_CALL_FLAGS_NONE,
                    100,
                    null,
                    null
                )
                return result != null && (g_variant_get_boolean(g_variant_get_child_value(result, 0u)) > 0)
            } catch (_: Exception) {
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
                g_dbus_proxy_call_sync(
                    proxy,
                    methodName,
                    null,
                    G_DBUS_CALL_FLAGS_NONE,
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
        ): CPointer<_GDBusProxy>? {
            val bus = g_bus_get_sync(G_BUS_TYPE_SYSTEM, null, null)
            return g_dbus_proxy_new_sync(
                bus,
                G_DBUS_PROXY_FLAGS_NONE,
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

