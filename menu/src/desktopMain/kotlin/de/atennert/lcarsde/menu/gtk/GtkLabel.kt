package de.atennert.lcarsde.menu.gtk

class GtkLabel(text: String) : GtkWidget(GTK.INSTANCE.gtk_label_new(text)) {
}