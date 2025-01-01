package de.atennert.gtk

class GtkButton(label: String? = null) : GtkWidget(GTK.INSTANCE.gtk_button_new()) {
    init {
        if (label != null) {
            GTK.INSTANCE.gtk_button_set_label(widget, label)
        }
    }

    fun setAlignment(xalign: Float, yalign: Float) {
        GTK.INSTANCE.gtk_button_set_alignment(widget, xalign, yalign)
    }

    fun onClick(callback: SignalCallback) {
        this.connect("clicked", callback)
    }
}