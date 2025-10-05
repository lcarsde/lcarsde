package de.atennert.gtk

class GtkButton(label: String? = null, private val button: ButtonRef = gtkButtonNew())
    : GtkWidget(button.toBWidgetRef()) {
    init {
        if (label != null) {
            gtkButtonSetLabel(button, label)
        }
    }

    fun setAlignment(xalign: Float, yalign: Float) {
        gtkButtonSetAlignment(button, xalign, yalign)
    }

    fun onClick(callback: CallbackRef, signalDataRef: SignalDataRef? = null) {
        this.connect("clicked", callback, signalDataRef)
    }
}