package de.atennert.lcarswm.window

import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.Atoms
import de.atennert.lcarswm.conversion.toUByteArray
import de.atennert.lcarswm.lifecycle.closeWith
import de.atennert.lcarswm.system.api.WindowUtilApi
import de.atennert.rx.NextObserver
import de.atennert.rx.operators.map
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Window

@ExperimentalForeignApi
fun updateWindowListAtom(
    rootWindow: Window,
    windowUtilApi: WindowUtilApi,
    atomLibrary: AtomLibrary,
    windowList: WindowList,
) {
    windowList.windowsObs
        .apply(map { windows -> windows.map { it.id } })
        .subscribe(NextObserver { windowIds ->
            windowUtilApi.changeProperty(
                rootWindow,
                atomLibrary[Atoms.NET_CLIENT_LIST],
                atomLibrary[Atoms.WINDOW],
                windowIds.toULongArray().toUByteArray(),
                32
            )
        })
        .closeWith { this.unsubscribe() }
}
