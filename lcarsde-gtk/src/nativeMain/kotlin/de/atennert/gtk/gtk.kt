@file:OptIn(ExperimentalForeignApi::class)

package de.atennert.gtk

import gtk.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.freeze

class NativeWidgetRef(val pointer: CPointer<gtk.GtkWidget>?)
class NativContainerRef(val pointer: CPointer<gtk.GtkContainer>?)
class NativScrolledWindowRef(val pointer: CPointer<GtkScrolledWindow>?)
class NativButtonRef(val pointer: CPointer<gtk.GtkButton>?)
class NativeStyleContextRef(val pointer: CValuesRef<GtkStyleContext>?)
class NativeCssProviderRef(val pointer: CPointer<GtkCssProvider>?)
class NativeStyleProviderRef(val pointer: CPointer<GtkStyleProvider>?)
class NativeSignalInstanceRef(val pointer: CPointer<out CPointed>?)
class NativeWindowRef(val pointer: CPointer<gtk.GtkWindow>?)
class NativeGdkWindowRef(val pointer: CPointer<GdkWindow>?)
class NativeBoxRef(val pointer: CPointer<gtk.GtkBox>?)
class NativeFlowBoxRef(val pointer: CPointer<gtk.GtkFlowBox>?)
class NativeCallbackRef(val pointer: CPointer<CFunction<() -> Unit>>)

actual typealias WidgetRef = NativeWidgetRef
actual typealias ContainerRef = NativContainerRef
actual typealias ScrolledWindowRef = NativScrolledWindowRef
actual typealias ButtonRef = NativButtonRef
actual typealias StyleContextRef = NativeStyleContextRef
actual typealias CssProviderRef = NativeCssProviderRef
actual typealias StyleProviderRef = NativeStyleProviderRef
actual typealias SignalInstanceRef = NativeSignalInstanceRef
actual typealias WindowRef = NativeWindowRef
actual typealias GdkWindowRef = NativeGdkWindowRef
actual typealias BoxRef = NativeBoxRef
actual typealias FlowBoxRef = NativeFlowBoxRef
actual typealias CallbackRef = NativeCallbackRef


@ExperimentalForeignApi
actual fun WidgetRef.toSignalInstanceRef(): SignalInstanceRef {
    return NativeSignalInstanceRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun CssProviderRef.toStyleProviderRef(): StyleProviderRef {
    return NativeStyleProviderRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun ContainerRef.toWidgetRef(): WidgetRef {
    return NativeWidgetRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun ScrolledWindowRef.toContainerRef(): ContainerRef {
    return NativContainerRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun ButtonRef.toBWidgetRef(): WidgetRef {
    return NativeWidgetRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun WindowRef.toWContainerRef(): ContainerRef {
    return NativContainerRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun BoxRef.toBContainerRef(): ContainerRef {
    return NativContainerRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
actual fun FlowBoxRef.toFBContainerRef(): ContainerRef {
    return NativContainerRef(this.pointer?.reinterpret())
}

@ExperimentalForeignApi
private val alignMapper = mapOf(
    GtkAlignment.FILL to GtkAlign.GTK_ALIGN_FILL,
    GtkAlignment.START to GtkAlign.GTK_ALIGN_START,
    GtkAlignment.END to GtkAlign.GTK_ALIGN_END,
    GtkAlignment.CENTER to GtkAlign.GTK_ALIGN_CENTER,
    GtkAlignment.BASELINE to GtkAlign.GTK_ALIGN_BASELINE,
)

@ExperimentalForeignApi
private val scrollPolicyMapper = mapOf(
    GtkPolicyType.NEVER to gtk.GtkPolicyType.GTK_POLICY_NEVER,
    GtkPolicyType.ALWAYS to gtk.GtkPolicyType.GTK_POLICY_ALWAYS,
    GtkPolicyType.AUTOMATIC to gtk.GtkPolicyType.GTK_POLICY_AUTOMATIC,
    GtkPolicyType.EXTERNAL to gtk.GtkPolicyType.GTK_POLICY_EXTERNAL
)

@ExperimentalForeignApi
private val windowTypeMapper = mapOf(
    GtkWindowType.TOPLEVEL to gtk.GtkWindowType.GTK_WINDOW_TOPLEVEL,
    GtkWindowType.POPUP to gtk.GtkWindowType.GTK_WINDOW_POPUP,
)

@ExperimentalForeignApi
private val orientationMapper = mapOf(
    GtkOrientation.HORIZONTAL to gtk.GtkOrientation.GTK_ORIENTATION_HORIZONTAL,
    GtkOrientation.VERTICAL to gtk.GtkOrientation.GTK_ORIENTATION_VERTICAL,
)

private fun Boolean.toInt() = if (this) 1 else 0

@ExperimentalForeignApi
actual fun gtkInit() = gtk_init(cValuesOf(0), cValue())

@ExperimentalForeignApi
actual fun gtkMainIteration() = gtk_main_iteration()

@ExperimentalForeignApi
actual fun gtkEventsPending() = gtk_events_pending()

@ExperimentalForeignApi
actual fun gtkLabelNew(text: String) = NativeWidgetRef(gtk_label_new(text))

@ExperimentalForeignApi
actual fun gtkCssProviderNew() = NativeCssProviderRef(gtk_css_provider_new())

@ExperimentalForeignApi
actual fun gtkCssProviderLoadFromPath(cssProvider: CssProviderRef, path: String) =
    gtk_css_provider_load_from_path(cssProvider.pointer, path, null)

@ExperimentalForeignApi
actual fun gtkWidgetGetStyleContext(widget: WidgetRef) =
    NativeStyleContextRef(gtk_widget_get_style_context(widget.pointer))

@ExperimentalForeignApi
actual fun gtkStyleContextAddProvider(
    styleContext: StyleContextRef,
    cssProvider: StyleProviderRef,
    priority: GtkStyleProviderPriority
) =
    gtk_style_context_add_provider(styleContext.pointer, cssProvider.pointer, priority.value.convert())

@ExperimentalForeignApi
actual fun gtkStyleContextAddClass(styleContext: StyleContextRef, cssClass: String) =
    gtk_style_context_add_class(styleContext.pointer, cssClass)

@ExperimentalForeignApi
actual fun gtkStyleContextRemoveClass(styleContext: StyleContextRef, cssClass: String) =
    gtk_style_context_remove_class(styleContext.pointer, cssClass)

@ExperimentalForeignApi
actual fun gtkStyleContextHasClass(styleContext: StyleContextRef, cssClass: String) =
    gtk_style_context_has_class(styleContext.pointer, cssClass) != 0

@ExperimentalForeignApi
actual fun gtkWidgetSetSizeRequest(widget: WidgetRef, width: Int, height: Int) =
    gtk_widget_set_size_request(widget.pointer, width, height)

@ExperimentalForeignApi
actual fun gtkWidgetSetHAlign(widget: WidgetRef, hAlign: GtkAlignment) =
    gtk_widget_set_halign(widget.pointer, alignMapper.getValue(hAlign))

@ExperimentalForeignApi
actual fun gtkWidgetSetVAlign(widget: WidgetRef, vAlign: GtkAlignment) =
    gtk_widget_set_valign(widget.pointer, alignMapper.getValue(vAlign))

@ExperimentalForeignApi
actual fun gtkWidgetShowAll(widget: WidgetRef) = gtk_widget_show_all(widget.pointer)

@ExperimentalForeignApi
actual fun gSignalConnectData(instance: SignalInstanceRef, signal: String, callback: CallbackRef) =
    g_signal_connect_data(
        instance.pointer,
        signal,
        callback.pointer,
        null,
        null,
        0u
    )

@ExperimentalForeignApi
actual fun gtkContainerAdd(parent: ContainerRef, child: WidgetRef) {
    gtk_container_add(parent.pointer, child.pointer)
}

@ExperimentalForeignApi
actual fun gtkContainerRemove(parent: ContainerRef, child: WidgetRef) {
    gtk_container_remove(parent.pointer, child.pointer)
}

@ExperimentalForeignApi
actual fun gtkScrolledWindowNew() =
    NativScrolledWindowRef(gtk_scrolled_window_new(null, null)?.reinterpret())

@ExperimentalForeignApi
actual fun gtkScrolledWindowSetPolicy(
    widget: ScrolledWindowRef,
    hScrollbarPolicy: GtkPolicyType,
    vScrollbarPolicy: GtkPolicyType
) =
    gtk_scrolled_window_set_policy(
        widget.pointer,
        scrollPolicyMapper.getValue(hScrollbarPolicy),
        scrollPolicyMapper.getValue(vScrollbarPolicy)
    )

@ExperimentalForeignApi
actual fun gtkButtonNew() = NativButtonRef(gtk_button_new()?.reinterpret())

@ExperimentalForeignApi
actual fun gtkButtonSetLabel(button: ButtonRef, label: String) =
    gtk_button_set_label(button.pointer, label)

@ExperimentalForeignApi
actual fun gtkButtonSetAlignment(button: ButtonRef, xalign: Float, yalign: Float) =
    gtk_button_set_alignment(button.pointer, xalign, yalign)

@ExperimentalForeignApi
actual fun gtkWindowNew(type: GtkWindowType): WindowRef =
    NativeWindowRef(gtk_window_new(windowTypeMapper.getValue(type))?.reinterpret())

@ExperimentalForeignApi
actual fun gtkWidgetGetWindow(window: WindowRef) =
    NativeGdkWindowRef(gtk_widget_get_window(window.pointer?.reinterpret()))

@ExperimentalForeignApi
actual fun gtkWindowSetTitle(window: WindowRef, title: String) = gtk_window_set_title(window.pointer, title)

@ExperimentalForeignApi
actual fun gdkX11WindowSetUtf8Property(window: GdkWindowRef, name: String, value: String) =
    gdk_x11_window_set_utf8_property(window.pointer, name, value)

@ExperimentalForeignApi
actual fun gtkBoxNew(orientation: GtkOrientation, spacing: Int) =
    NativeBoxRef(gtk_box_new(orientationMapper.getValue(orientation), spacing)?.reinterpret())

@ExperimentalForeignApi
actual fun gtkBoxPackStart(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt) =
    gtk_box_pack_start(box.pointer, child.pointer, expand.toInt(), fill.toInt(), padding)

@ExperimentalForeignApi
actual fun gtkBoxPackEnd(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt) =
    gtk_box_pack_end(box.pointer, child.pointer, expand.toInt(), fill.toInt(), padding)

@ExperimentalForeignApi
actual fun gtkFlowBoxNew() = NativeFlowBoxRef(gtk_flow_box_new()?.reinterpret())
