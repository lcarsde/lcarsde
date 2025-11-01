# lcarsde

LCARS Desktop Environment, official project name lcarsde, or for logos also LCARS DE, is a desktop environment that
mimics the LCARS interface.

## Sub projects

* [lcarswm - LCARS Window Manager](lcarswm/readme.md)
* [logout - tool for loging out and shutting computer down](logout/readme.md)
* [menu - sidebar menu application for selecting and closing open windows](menu/readme.md)
* [status-bar - status bar tool application for general information display and controls](status-bar/readme.md)
* [app-selector - application for opening installed programs](app-selector/readme.md)
* [lcarsde-common](lcarsde-common/readme.md)
* [lcarsde-gtk](lcarsde-gtk/readme.md)
* [lcarsde.github.io - lcarsde website with news and instructions](https://github.com/lcarsde/lcarsde.github.io)

## Architecture Decision Records (ARDs)

[Follow this link to the ADRs](docs/adr/adr-index.md)

## To Do

* lcarswm
    * Associate child screens with their parents
    * Identify and handle popups as popups
    * Center popups and adjust the frame to their size
    * Monitors
        * Merge overlaying screens of the same size
        * If screens have different sizes, the higher one draws
    * check for XDG-path variables and have a fallback
    * basic configurable window tiling
* status-bar
    * System tray widget
    * Content for the data bar (empty upper area in normal mode)
        * Data throughput of network interfaces
        * Bluetooth
        * ...
* general
    * Configuration for the colors
    * Pressed state coloring for the buttons
    * GTK-Theme (if I can't find one)
    * Detailed documentation for communication between lcarsde applications
        * Message pipes between lcarswm and the application menu
        * Special atoms for the application menu and status bar
    * lcarsde settings application
    * lcarsde file manager
    * polkit agent
    * lcarsde session manager
        * set up which apps to autostart
