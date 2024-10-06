# Identification of special tool windows

* Status: accepted
* Deciders: Andreas Tennert
* Date: 2020-04-??

## Context and Problem Statement

Some tool windows like the status bar and the side bar menu need special placement in the UI and therefore special treatment by the window manager. The window manager must be able to identify them to treat them accordingly.

## Considered Options

* Client Properties / Atoms
* Startup order
* POSIX-IPC communication

## Decision Outcome

Chosen option: "Client Properties / Atoms", because it is the most reliable and side-effect free way to identify the tool windows.

## Pros and Cons of the Options <!-- optional -->

### Client Properties / Atoms

* Good, because the property is already available in the window manager at the time of window setup.
* Good, because lot's of properties are read on window manager side anyway so that is a standard method here
* Bad, because needs use of Xlib methods to set property additional to use of UI-framework

### Startup order

* Good, because no extra handling in client required
* Bad, because the startup order of the tools needs to be guaranteed. That is not good, because that requires knowledge of this by every OS creator and user.

### POSIX-IPC communication

* Good, because no Xlib specific handling
* Bad, because communication needs to be synchronized with window setup. It might come to flickering, if the window is drawn before the communication is done.

## Links <!-- optional -->

* [Xlib manual - Properties and Atoms](https://tronche.com/gui/x/xlib/window-information/properties-and-atoms.html)
* [ICCCM - Atoms](https://tronche.com/gui/x/icccm/sec-1.html#s-1.2)
