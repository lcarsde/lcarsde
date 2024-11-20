package de.atennert.lcarsde.menu.gtk

import de.atennert.lcarsde.menu.Uint32_t

class GtkScrollContainer : GtkContainer(GTK.INSTANCE.gtk_scrolled_window_new(null, null)) {
    fun setPolicy(hScrollbarPolicy: GtkPolicyType, vScrollbarPolicy: GtkPolicyType) {
        GTK.INSTANCE.gtk_scrolled_window_set_policy(
            widget,
            Uint32_t(hScrollbarPolicy.value),
            Uint32_t(vScrollbarPolicy.value)
        )
    }
}
