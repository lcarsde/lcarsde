package de.atennert.gtk

import gtk._GtkCssProvider
import gtk.gtk_css_provider_load_from_path
import gtk.gtk_css_provider_new
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class CssProvider private constructor(internal val ref: CPointer<_GtkCssProvider>?) {

    private fun loadFromPath(path: String) = gtk_css_provider_load_from_path(this.ref, path, null)

    companion object {
        fun fromPath(path: String): CssProvider {
            val cssProvider = CssProvider(gtk_css_provider_new())
            cssProvider.loadFromPath(path)
            return cssProvider
        }
    }
}