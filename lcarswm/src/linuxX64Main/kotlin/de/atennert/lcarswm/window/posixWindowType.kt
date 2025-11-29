package de.atennert.lcarswm.window

import de.atennert.lcarswm.atom.Atoms

val TRANSIENT_WINDOW_TYPES = setOf(WindowType.DIALOG, WindowType.TOOLBAR, WindowType.UTILITY)

val WINDOW_TYPE_ATOM_MAP = mapOf(
    WindowType.NORMAL to Atoms.NET_WM_WINDOW_TYPE_NORMAL,
    WindowType.DESKTOP to Atoms.NET_WM_WINDOW_TYPE_DESKTOP,
    WindowType.DIALOG to Atoms.NET_WM_WINDOW_TYPE_DIALOG,
    WindowType.DOCK to Atoms.NET_WM_WINDOW_TYPE_DOCK,
    WindowType.SPLASH to Atoms.NET_WM_WINDOW_TYPE_SPLASH,
    WindowType.OVERRIDE to Atoms.NET_WM_WINDOW_TYPE_OVERRIDE,
    WindowType.TOOLBAR to Atoms.NET_WM_WINDOW_TYPE_TOOLBAR,
    WindowType.UTILITY to Atoms.NET_WM_WINDOW_TYPE_UTILITY,
)
