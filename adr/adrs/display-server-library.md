# Display Server Library

* Status: accepted <!-- optional -->
* Deciders: Andreas Tennert <!-- optional -->
* Date: 2019-08-11 <!-- optional -->

## Context and Problem Statement

The window manager needs to use some kind of display management system to get notified about opening and closing windows and generally handle functions on the display and input/output. XCB was used initially but some additional functionality was needed, which is covered by Xlib libraries. Mixing the two systems became a hazard.

## Considered Options

* XCB
* Xlib
* Wayland

## Decision Outcome

Chosen option: "Xlib", because it was used already for the additional tooling and there's lots of documentations and examples.

### Positive Consequences <!-- optional -->

* Direct compatibility with additional tool libraries, e.g. for XPM handling
* Lots of resources and with Openbox a very complete WM (in sense of ICCCM and EWMH) as possible template

### Negative Consequences <!-- optional -->

* Refactoring code
* Wayland might be more modern
* XCB is faster at times due to asynchronous

## Pros and Cons of the Options <!-- optional -->

### XCB

* Good, because faster due to asynchronous and more fine granular handling
* Bad, because missing functionality that is available in XCB library

### Xlib

* Good, because lots of functionality in libraries
* Good, because broadly in use
* Bad, because old and not easy to use due to lacking specification (need to additionally have ICCCM and EWMH)

### Wayland

* Good, because new and maybe better and gaining popularity
* Bad, because I don't know it at all and I don't want to learn this also right now
