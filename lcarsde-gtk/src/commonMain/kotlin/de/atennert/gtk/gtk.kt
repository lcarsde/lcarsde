package de.atennert.gtk

expect class WidgetRef
expect class ContainerRef
expect class ScrolledWindowRef
expect class ButtonRef
expect class StyleContextRef
expect class CssProviderRef
expect class StyleProviderRef
expect class SignalInstanceRef
expect class WindowRef
expect class GdkWindowRef
expect class BoxRef
expect class FlowBoxRef
expect class CallbackRef
expect class SignalDataRef

expect fun WidgetRef.toSignalInstanceRef(): SignalInstanceRef
expect fun CssProviderRef.toStyleProviderRef(): StyleProviderRef
expect fun ContainerRef.toWidgetRef(): WidgetRef
expect fun ScrolledWindowRef.toContainerRef(): ContainerRef
expect fun ButtonRef.toBWidgetRef(): WidgetRef
expect fun WindowRef.toWContainerRef(): ContainerRef
expect fun BoxRef.toBContainerRef(): ContainerRef
expect fun FlowBoxRef.toFBContainerRef(): ContainerRef


expect fun gtkInit()

expect fun gtkMainIteration(): Int

expect fun gtkEventsPending(): Int

expect fun gtkLabelNew(text: String): WidgetRef

expect fun gtkCssProviderNew(): CssProviderRef

expect fun gtkCssProviderLoadFromPath(cssProvider: CssProviderRef, path: String): Int

expect fun gtkWidgetGetStyleContext(widget: WidgetRef): StyleContextRef

expect fun gtkStyleContextAddProvider(styleContext: StyleContextRef, cssProvider: StyleProviderRef, priority: GtkStyleProviderPriority)

expect fun gtkStyleContextAddClass(styleContext: StyleContextRef, cssClass: String)

expect fun gtkStyleContextRemoveClass(styleContext: StyleContextRef, cssClass: String)

expect fun gtkStyleContextHasClass(styleContext: StyleContextRef, cssClass: String): Boolean

expect fun gtkWidgetSetSizeRequest(widget: WidgetRef, width: Int, height: Int)

expect fun gtkWidgetSetHAlign(widget: WidgetRef, hAlign: GtkAlignment)

expect fun gtkWidgetSetVAlign(widget: WidgetRef, vAlign: GtkAlignment)

expect fun gtkWidgetShowAll(widget: WidgetRef)

expect fun gSignalConnectData(instance: SignalInstanceRef, signal: String, callback: CallbackRef, signalDataRef: SignalDataRef? = null): ULong

expect fun gtkContainerAdd(parent: ContainerRef, child: WidgetRef)

expect fun gtkContainerRemove(parent: ContainerRef, child: WidgetRef)

expect fun gtkScrolledWindowNew(): ScrolledWindowRef

expect fun gtkScrolledWindowSetPolicy(widget: ScrolledWindowRef, hScrollbarPolicy: GtkPolicyType, vScrollbarPolicy: GtkPolicyType)

expect fun gtkButtonNew(): ButtonRef

expect fun gtkButtonSetLabel(button: ButtonRef, label: String)

expect fun gtkButtonSetAlignment(button: ButtonRef, xalign: Float, yalign: Float)

expect fun gtkWindowNew(type: GtkWindowType): WindowRef

expect fun gtkWidgetGetWindow(window: WindowRef): GdkWindowRef

expect fun gtkWindowSetTitle(window: WindowRef, title: String)

expect fun gdkX11WindowSetUtf8Property(window: GdkWindowRef, name: String, value: String)

expect fun gtkBoxNew(orientation: GtkOrientation, spacing: Int): BoxRef

expect fun gtkBoxPackStart(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt)

expect fun gtkBoxPackEnd(box: BoxRef, child: WidgetRef, expand: Boolean, fill: Boolean, padding: UInt)

expect fun gtkFlowBoxNew(): FlowBoxRef
