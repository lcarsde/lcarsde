package de.atennert.gtk

import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import de.atennert.Uint32_t

class JvmReference(val pointer: Pointer)
class CallbackContainer(val callback: SignalCallback)

actual typealias WidgetRef = JvmReference
actual typealias ContainerRef = JvmReference
actual typealias ScrolledWindowRef = JvmReference
actual typealias ButtonRef = JvmReference
actual typealias StyleContextRef = JvmReference
actual typealias CssProviderRef = JvmReference
actual typealias StyleProviderRef = JvmReference
actual typealias SignalInstanceRef = JvmReference
actual typealias WindowRef = JvmReference
actual typealias GdkWindowRef = JvmReference
actual typealias BoxRef = JvmReference
actual typealias FlowBoxRef = JvmReference
actual typealias CallbackRef = CallbackContainer


actual fun WidgetRef.toSignalInstanceRef(): SignalInstanceRef = this
actual fun CssProviderRef.toStyleProviderRef(): StyleProviderRef = this
actual fun ContainerRef.toWidgetRef(): WidgetRef = this
actual fun ScrolledWindowRef.toContainerRef(): ContainerRef = this
actual fun ButtonRef.toBWidgetRef(): WidgetRef = this
actual fun WindowRef.toWContainerRef(): ContainerRef = this
actual fun BoxRef.toBContainerRef(): ContainerRef = this
actual fun FlowBoxRef.toFBContainerRef(): ContainerRef = this


actual fun gtkInit() = GTK.INSTANCE.gtk_init(IntByReference(0), null)

actual fun gtkMainIteration() = GTK.INSTANCE.gtk_main_iteration()

actual fun gtkEventsPending() = GTK.INSTANCE.gtk_events_pending()

actual fun gtkLabelNew(text: String) = JvmReference(GTK.INSTANCE.gtk_label_new(text))

actual fun gtkCssProviderNew() = JvmReference(GTK.INSTANCE.gtk_css_provider_new())

actual fun gtkCssProviderLoadFromPath(cssProvider: CssProviderRef, path: String): Int =
    GTK.INSTANCE.gtk_css_provider_load_from_path(cssProvider.pointer, path, null)

actual fun gtkWidgetGetStyleContext(widget: WidgetRef) =
    JvmReference(GTK.INSTANCE.gtk_widget_get_style_context(widget.pointer))

actual fun gtkStyleContextAddProvider(styleContext: StyleContextRef, cssProvider: StyleProviderRef, priority: GtkStyleProviderPriority) =
    GTK.INSTANCE.gtk_style_context_add_provider(
        styleContext.pointer,
        cssProvider.pointer,
        Uint32_t(priority.value)
    )

actual fun gtkStyleContextAddClass(styleContext: StyleContextRef, cssClass: String) =
    GTK.INSTANCE.gtk_style_context_add_class(styleContext.pointer, cssClass)

actual fun gtkStyleContextRemoveClass(styleContext: StyleContextRef, cssClass: String) =
    GTK.INSTANCE.gtk_style_context_remove_class(styleContext.pointer, cssClass)

actual fun gtkStyleContextHasClass(styleContext: StyleContextRef, cssClass: String) =
    GTK.INSTANCE.gtk_style_context_has_class(styleContext.pointer, cssClass)

actual fun gtkWidgetSetSizeRequest(widget: WidgetRef, width: Int, height: Int) =
    GTK.INSTANCE.gtk_widget_set_size_request(widget.pointer, width, height)

actual fun gtkWidgetSetHAlign(widget: WidgetRef, hAlign: GtkAlignment) =
    GTK.INSTANCE.gtk_widget_set_halign(widget.pointer, hAlign.value)

actual fun gtkWidgetSetVAlign(widget: WidgetRef, vAlign: GtkAlignment) =
    GTK.INSTANCE.gtk_widget_set_valign(widget.pointer, vAlign.value)

actual fun gtkWidgetShowAll(widget: WidgetRef) = GTK.INSTANCE.gtk_widget_show_all(widget.pointer)

actual fun gSignalConnectData(instance: SignalInstanceRef, signal: String, callback: CallbackRef) =
    GObject.INSTANCE.g_signal_connect_data(
        instance.pointer,
        signal,
        callback.callback,
        null,
        null,
        Uint32_t(0)
    ).toLong().toULong()

actual fun gtkContainerAdd(parent: ContainerRef, child: WidgetRef) =
    GTK.INSTANCE.gtk_container_add(parent.pointer, child.pointer)

actual fun gtkContainerRemove(parent: ContainerRef, child: WidgetRef) =
    GTK.INSTANCE.gtk_container_remove(parent.pointer, child.pointer)

actual fun gtkScrolledWindowNew() = JvmReference(GTK.INSTANCE.gtk_scrolled_window_new(null, null))

actual fun gtkScrolledWindowSetPolicy(
    widget: ScrolledWindowRef,
    hScrollbarPolicy: GtkPolicyType,
    vScrollbarPolicy: GtkPolicyType
) =
    GTK.INSTANCE.gtk_scrolled_window_set_policy(
        widget.pointer,
        Uint32_t(hScrollbarPolicy.value),
        Uint32_t(vScrollbarPolicy.value)
    )

actual fun gtkButtonNew(): ButtonRef = JvmReference(GTK.INSTANCE.gtk_button_new())

actual fun gtkButtonSetLabel(button: ButtonRef, label: String) =
    GTK.INSTANCE.gtk_button_set_label(button.pointer, label)

actual fun gtkButtonSetAlignment(button: ButtonRef, xalign: Float, yalign: Float) =
    GTK.INSTANCE.gtk_button_set_alignment(button.pointer, xalign, yalign)

actual fun gtkWindowNew(type: GtkWindowType) =
    JvmReference(GTK.INSTANCE.gtk_window_new(Uint32_t(type.value)))

actual fun gtkWidgetGetWindow(window: WindowRef) = JvmReference(GTK.INSTANCE.gtk_widget_get_window(window.pointer))

actual fun gtkWindowSetTitle(window: WindowRef, title: String) =
    GTK.INSTANCE.gtk_window_set_title(window.pointer, title)

actual fun gdkX11WindowSetUtf8Property(window: GdkWindowRef, name: String, value: String) =
    GDK.INSTANCE.gdk_x11_window_set_utf8_property(window.pointer, name, value)

actual fun gtkBoxNew(orientation: GtkOrientation, spacing: Int): BoxRef =
    JvmReference(GTK.INSTANCE.gtk_box_new(Uint32_t(orientation.value), spacing))

actual fun gtkBoxPackStart(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt) =
    GTK.INSTANCE.gtk_box_pack_start(box.pointer, child.pointer, expand, fill, Uint32_t(padding.toInt()))

actual fun gtkBoxPackEnd(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt) =
    GTK.INSTANCE.gtk_box_pack_end(box.pointer, child.pointer, expand, fill, Uint32_t(padding.toInt()))

actual fun gtkFlowBoxNew() = JvmReference(GTK.INSTANCE.gtk_flow_box_new())
