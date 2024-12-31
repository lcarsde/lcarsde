package de.atennert.gtk

class GtkLabel(text: String) : GtkWidget(GTK.INSTANCE.gtk_label_new(text)) {
}