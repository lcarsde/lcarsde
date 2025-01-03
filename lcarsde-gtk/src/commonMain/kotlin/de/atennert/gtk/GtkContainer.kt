package de.atennert.gtk

open class GtkContainer(private val container: ContainerRef) : GtkWidget(container.toWidgetRef()) {
    fun add(child: GtkWidget) {
        gtkContainerAdd(this.container, child.widget)
    }

    fun remove(child: GtkWidget) {
        gtkContainerRemove(this.container, child.widget)
    }
}