package de.atennert.lcarswm.atom

import de.atennert.lcarswm.system.api.WindowUtilApi
import kotlinx.cinterop.ExperimentalForeignApi
import xlib.Atom

/**
 * This class is supposed to hold the required X atoms and provide them to other classes.
 */
@ExperimentalForeignApi
class AtomLibrary(windowUtils: WindowUtilApi) {

    /**
     * Map of atoms that are used by the window manager. The atoms are initialized at start.
     */
    private val atomMap: Map<Atoms, Atom> = Atoms.values().asList().associateWith { windowUtils.internAtom(it.atomName) }

    /**
     * Get the atom value for an atom enum value
     */
    operator fun get(atom: Atoms): Atom = atomMap.getValue(atom)
}