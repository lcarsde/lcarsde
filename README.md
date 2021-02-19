# lcarsde
LCARS Desktop Environment, official project name lcarsde, or for logos also LCARS DE, is a desktop environment that mimics the LCARS interface.

## Sub projects
* [lcarswm - LCARS Window Manager](https://github.com/lcarsde/lcarswm)
* [menu - sidebar menu application for selecting and closing open windows](https://github.com/lcarsde/menu)
* [status-bar - status bar tool application for general information display and controls](https://github.com/lcarsde/status-bar)
* [application-starter - application for opening installed programs](https://github.com/lcarsde/application-starter)

## Architecture Decision Records (ARDs)
[Follow this link to the ADRs](adr/adr-index.md)

## To Do
* lcarswm
  * Associate child screens with their parents
  * Identify and handle popups as popups
  * Center popups and adjust the frame to their size
  * Monitors
    * Merge overlaying screens of same size
    * If screens have different sizes, the higher one draws
  * check for XDG-path variables and have a fallback
  * basic configurable window tiling
  * move windows via drag-and-drop on the title bar
* status-bar
  * Content for the data bar (empty upper area in normal mode)
    * Memory usage
    * Data throughput of network interfaces
    * Bluetooth
    * ...
* general
  * Configuration for the colors
  * Pressed state coloring for the buttons
  * Continuous Deployment
    * Ubuntu
    * Arch Linux
  * GTK-Theme (if I can't find one)
  * Detailed documentation for communication between lcarsde applications
    * Message pipes between lcarswm and application menu
    * Special atoms for application menu and status bar
  * Settings application
