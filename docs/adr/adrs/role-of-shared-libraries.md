# Role of the shared libraries

* Status: accepted
* Deciders: Andreas Tennert
* Date: 2026-10-15

## Context and Problem Statement

lcarsde has two shared libraries, lcarsde-common and lcarsde-gtk. lcarsde-gtk is a thin set of GTK3 wrappers, so every Tool App styles itself and sets itself up on its own. This has led to copied code between the Tool Apps (colors, GTK helpers, the `main()`/application bootstrap). More Tool Apps are planned (e.g. settings, file manager), and all of them shall match the look of the Window Manager. What is each shared library responsible for?

## Decision Drivers

* New Tool Apps shall be cheap to build and look consistent with the Window Manager
* Code shared by the Window Manager and the Tool Apps must not pull in GTK
* No copied code between Tool Apps

## Considered Options

1. lcarsde-gtk stays a thin Kotlin binding over GTK; each Tool App does its own styling and setup
2. lcarsde-gtk becomes the lcarsde window toolkit (module renamed to lcarsde-window-toolkit): GTK binding plus shared theme, components and Tool App bootstrap

## Decision Outcome

Chosen option: "2", because it makes building further consistently styled Tool Apps cheap and gives a clear home for code that is currently copied between Tool Apps.

The responsibilities are:

* **lcarsde-common**: GTK-free code shared by the Window Manager and the Tool Apps: IPC, logging, process handling, file and settings reading.
* **lcarsde-window-toolkit** (formerly lcarsde-gtk): everything a Tool App needs for its UI: the GTK binding, the shared theme (colors, fonts, CSS), reusable lcarsde-styled components, and the Tool App bootstrap. A new Tool App should mostly consist of its own layout and logic.

### Positive Consequences

* Consistent look across all Tool Apps
* Less boilerplate per Tool App
* Clear destination when moving copied code out of the Tool Apps

### Negative Consequences

* The toolkit's API becomes a dependency of every Tool App; changes to it ripple through all of them
* Existing Tool Apps need to be migrated

## Pros and Cons of the Options

### Thin binding

* Good, because the library stays small and generic
* Bad, because theme and setup code keep getting copied into every Tool App
* Bad, because look and behavior of Tool Apps drift apart

### lcarsde window toolkit

* Good, because new Tool Apps are mostly layout and logic
* Good, because the look is defined in one place
* Bad, because the library grows and needs a deliberately designed API

## Links

* Refines [Project Merge](project-merge.md)
