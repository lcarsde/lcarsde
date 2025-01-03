package de.atennert.gtk

open class GtkWidget(val widget: WidgetRef) {
    fun setStyling(cssProvider: CssProviderRef, vararg classes: String) {
        val styleContext = gtkWidgetGetStyleContext(widget)
        for (cssClass in classes) {
            gtkStyleContextAddClass(styleContext, cssClass)
        }
        gtkStyleContextAddProvider(styleContext, cssProvider.toStyleProviderRef(), GtkStyleProviderPriority.USER)
    }

    fun addClass(cssClass: String) {
        val styleContext = gtkWidgetGetStyleContext(widget)
        gtkStyleContextAddClass(styleContext, cssClass)
        this.showAll()
    }

    fun removeClass(cssClass: String) {
        val styleContext = gtkWidgetGetStyleContext(widget)
        gtkStyleContextRemoveClass(styleContext, cssClass)
        this.showAll()
    }

    fun hasClass(cssClass: String): Boolean {
        val styleContext = gtkWidgetGetStyleContext(widget)
        return gtkStyleContextHasClass(styleContext, cssClass)
    }

    fun setSize(width: Int, height: Int) {
        gtkWidgetSetSizeRequest(widget, width, height)
    }

    fun showAll() {
        gtkWidgetShowAll(widget)
    }

    fun connect(signal: String, callback: () -> Unit) {
        gSignalConnectData(widget.toSignalInstanceRef(), signal, callback)
    }

    fun setHAlign(hAlign: GtkAlignment) {
        gtkWidgetSetHAlign(widget, hAlign)
    }

    fun setVAlign(vAlign: GtkAlignment) {
        gtkWidgetSetVAlign(widget, vAlign)
    }

    fun setAlign(hAlign: GtkAlignment, vAlign: GtkAlignment) {
        setHAlign(hAlign)
        setVAlign(vAlign)
    }
}
