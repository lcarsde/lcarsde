package de.atennert.gtk

open class GtkBox(
    orientation: GtkOrientation, spacing: Int,
    private val box: BoxRef = gtkBoxNew(orientation, spacing)
) : GtkContainer(box.toBContainerRef()) {

    fun packStart(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        gtkBoxPackStart(
            box,
            child.widget,
            expand,
            fill,
            padding
        )
    }

    fun packEnd(child: GtkWidget, expand: Boolean, fill: Boolean, padding: UInt) {
        gtkBoxPackEnd(
            box,
            child.widget,
            expand,
            fill,
            padding
        )
    }
}
