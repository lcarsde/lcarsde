# lcarsde

lcarsde is a desktop environment for X11 with an LCARS-like look. It consists of a window manager plus a set of companion programs that together form the desktop.

## Language

### The desktop environment

**lcarsde**:
The desktop environment as a whole: the Window Manager together with its Tool Apps.
_Avoid_: the project, LCARS

**Window Manager**:
The lcarswm process, which owns the X display, manages all windows and draws the lcarsde Layout. "lcarswm" is used only when referring to the module or binary.
_Avoid_: core, core app, WM (in prose)

**Tool App**:
A program shipped as part of lcarsde, styled in the lcarsde look, that complements the Window Manager, e.g. the Status Bar, the Side Bar or the Logout. Most Tool Apps show ordinary Application Windows; only some have Tool Windows.
_Avoid_: tool, helper app, client

**Tool Window**:
A window that the Window Manager identifies and treats specially: it is placed in a Slot, gets no Frame and no focus handling. Defined by how the Window Manager treats it, not by which program created it.
_Avoid_: special window, dock window

**Side Bar**:
The Tool App in the Side Bar Slot; its window is a Tool Window. Currently it lists the open Application Windows for selecting and closing them.
_Avoid_: app menu, menu, window list

**Status Bar**:
The Tool App showing time, system and hardware status in the lcarsde Layout; its window is a Tool Window.

**App Selector**:
The Tool App for launching Applications.
_Avoid_: application starter, launcher

**Logout**:
The Tool App for ending the session (logging out, locking, shutting down).
_Avoid_: logout screen

**Tool App Protocol**:
The conventions by which the Window Manager and Tool Apps communicate: how Tool Windows identify themselves, and the messages exchanged between them (e.g. the window list sent to the Side Bar, and the select and close requests sent back).
_Avoid_: IPC (in domain language), message queue protocol, app menu protocol

### Screens

**Screen**:
A physical display surface the desktop is shown on, e.g. a notebook panel, a tablet display or an external monitor. Backed by a RandR output.
_Avoid_: monitor, display, output (output is reserved for the RandR level), and "screen" in the X11 sense (say **X Screen** for that)

**Primary Screen**:
The Screen marked as primary; the only Screen with a dedicated Normal layout and Slots.
_Avoid_: main screen, primary monitor

**Tile** _(planned, not yet implemented)_:
A rectangular part of a Screen that holds a stack of Application Windows and shows one of them. Every Screen has at least one Tile; further Tiles are created only by the user deliberately splitting, never automatically when windows open.
_Avoid_: screen tile, section, area, container

**Front Window** _(planned, not yet implemented)_:
The Application Window a Tile currently shows on top of its stack. Every non-empty Tile has exactly one; the Focused Window is always the Front Window of its Tile.
_Avoid_: top window, visible window, current window

**Focused Tile** _(planned, not yet implemented)_:
The Tile containing the Focused Window; new Application Windows open there.

**Screen Mode**:
The desktop-wide setting for how much Decoration the Window Manager draws: Normal, Maximized or Fullscreen. It applies to all Screens at once.
_Avoid_: monitor mode, layout mode, window mode

**Normal**:
The Screen Mode with the full lcarsde Layout. Only the Primary Screen has a dedicated Normal layout (including the Tool Windows); on all other Screens, Normal looks the same as Maximized.

**Maximized**:
The Screen Mode with reduced decoration: only a thin bar above and below the Application Windows remains, and the Tool Windows are hidden.

**Fullscreen**:
The Screen Mode without Decoration: Application Windows fill the whole Screen and the Tool Windows are hidden.

**X Screen**:
The X11 protocol's screen, which spans all Screens combined and owns the root window. Only relevant at the X11 protocol layer.

### lcarsde Layout

**lcarsde Layout**:
The LCARS-like arrangement of bars, elbows and Slots that the Window Manager draws on the Screens, depending on the Screen Mode.
_Avoid_: LCARS layout, LCARS interface

**Decoration**:
Everything the Window Manager draws itself: the lcarsde Layout and the Frames.
_Avoid_: chrome, LCARS decoration

**Slot**:
A dedicated region of the lcarsde Layout, drawn by the Window Manager, in which a Tool Window is placed. Every Tool Window lives in a Slot.
_Avoid_: dock, panel, area

**Side Bar Slot**:
The Slot in the left column of the Primary Screen's Normal layout that holds the Side Bar.
_Avoid_: side bar (for the region)

**Status Bar Slot**:
The Slot at the top of the Primary Screen's Normal layout that holds the Status Bar.
_Avoid_: data area

**Frame**:
The Decoration the Window Manager draws around an Application Window, including its Title Bar.
_Avoid_: border

**Title Bar**:
The part of a Frame that shows the Application Window's title and is used to move it.

### Managed programs

**Application**:
A program not part of lcarsde whose windows the Window Manager manages, e.g. a browser or a terminal.
_Avoid_: client, program

**Application Window**:
A top-level window that the Window Manager manages on behalf of an Application: framed, placed, focused and listed.
_Avoid_: client, client window ("client" is reserved for the X11 protocol layer)

**Focused Window**:
The single Application Window, desktop-wide, that currently receives keyboard input.
_Avoid_: active window, current window

**Window Cycling**:
Moving the focus through the Application Windows one by one via a held key combination.
_Avoid_: key session, alt-tab
