package de.atennert.gtk

class GtkScrollContainer(private val scrollWindow: ScrolledWindowRef = gtkScrolledWindowNew())
    : GtkContainer(scrollWindow.toContainerRef()) {
    fun setPolicy(hScrollbarPolicy: GtkPolicyType, vScrollbarPolicy: GtkPolicyType) {
        gtkScrolledWindowSetPolicy(scrollWindow, hScrollbarPolicy, vScrollbarPolicy)
    }
}
