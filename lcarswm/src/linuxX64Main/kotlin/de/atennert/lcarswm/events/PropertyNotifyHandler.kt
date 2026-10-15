package de.atennert.lcarswm.events

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import gtk.PropertyNotify
import gtk.XEvent
import kotlinx.cinterop.ExperimentalForeignApi

@ExperimentalForeignApi
class PropertyNotifyHandler(
    private val atomLibrary: AtomLibrary,
    private val eventStore: EventStore,
) : XEventHandler {
    override val xEventType = PropertyNotify

    override fun handleEvent(event: XEvent): Boolean {
        when (event.xproperty.atom) {
            atomLibrary[Atoms.WM_NAME],
            atomLibrary[Atoms.NET_WM_NAME] -> eventStore.propertyNotifyNameSj.next(event.xproperty.window)
        }
        return false
    }
}