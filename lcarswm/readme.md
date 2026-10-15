# lcarswm
lcarswm or LCARS Window Manager is a window manager written in Kotlin that is designed to look like an LCARS interface.

lcarswm has two kinds of Screens, one Primary Screen (which maps to the RandR primary output) and "other" Screens. Only the Primary Screen has the Status Bar Slot and the Side Bar Slot that holds the Side Bar. The Slots are only visible in the Normal Screen Mode. There are three screen modes:
* Normal mode: Side Bar and Status Bar Slots on the Primary Screen, upper and lower bars on the other Screens
* Maximized mode: upper and lower bars on all Screens
* Fullscreen mode: no bars, windows are maximized

If there's no primary Screen defined in the X server, then lcarswm will use the first Screen it finds as primary.

lcarswm is a stacking window manager and displays one window at a time per Screen. Windows always open on the Primary Screen and can be moved to other Screens with keyboard short cuts.

So far the window manager interaction is mostly based on key commands, as listed below. However windows can also be switched and closed using mouse actions by either clicking on the window or using the Side Bar.

The Side Bar is displayed in the Side Bar Slot on the Primary Screen in Normal mode. The Side Bar shows all open windows and can be used to select windows or close them with mouse actions.

Checkout https://www.andreas-tennert.de/#2-0006 for images :-)

## Copyright
Copyright (C) 2019  Andreas Tennert

## Acknowledgements
Thank you very much to the creators of the following resources:
* [Chuan Ji - How X Window Managers Work, And How To Write One](https://jichu4n.com/posts/how-x-window-managers-work-and-how-to-write-one-part-i/)
* [openbox on github](https://github.com/danakj/openbox)
* [mcwm on GitHub](https://github.com/mchackorg/mcwm)
* [XCB implementation of TinyWM on GitHub](https://github.com/stefanbidi/tinywm-xcb)
* [XCB tutorial](https://www.x.org/releases/X11R7.7/doc/libxcb/tutorial/index.html)
* [How to read XPM files and draw them in a window](http://www.dis.uniroma1.it/~liberato/screensaver/image.html)
* [libXpm API](https://fossies.org/dox/libXpm-4.7/files.html)

## Functionality
* Window title bar, with window name and different colors for active and inactive windows
* Side Bar in the Normal Screen Mode on the Primary Screen that lists all active windows with
  * a window selection button (blue with program name)
  * a close button (red, small without text)
* Windows are resized when the screen resolution is changed and the size depends on the window manager mode.
* Windows are always opened on the Primary Screen
* If a Screen is removed, its windows are moved to the Primary Screen
* Autostart applications from *.desktop files in `/etc/xdg/autostart/` and `~/.config/autostart/`
* The settings file contains key handling and general settings per user and is created as `~/.config/lcarswm/settings.xml` if it doesn't exist and can be edited afterwards
  * Key handling
    * Windows + Q closes the window manager
    * Windows + M toggles the display mode between normal, maximized and fullscreen
    * Alt + Tab toggles through the windows
    * Alt + Shift + Tab toggles backwards through the windows
    * Alt + Windows + Up/Down moves the active window to other Screens
    * Alt + F4 closes the active window
    * Other key combinations can be connected to commands via a settings file
    * Modifier keys are
      * Shift
      * Ctrl
      * Alt
      * Win / Lin / Super
      * Meta
      * Hyper
    * By clicking and holding a window title bar with the left mouse button and dragging the window can be moved between Screens
  * General settings
    * title: title that is displayed in the top bar in normal and maximized mode
    * title-image: optional, path to image in XPM format, if set, it will be shown instead of the title. Should have a height of 40px and be at least 16px shorter then the normal modes' top bar.
    * font: the font used for drawing the title and the window titles

## Required
### For compiling
* header files and development libraries for XLib, randr, xpm, libxml2, glib and pango. Check the [circle ci config file](.circleci/config.yml) to find the build dependencies in the install routines for Ubuntu.

### For using
* Ubuntu Condensed font: It comes close enough to LCARS letters and is used by lcarswm as default for writing.
* Libraries for XLib, randr, xpm, libxml2, glib and pango. Check the [circle ci config file](.circleci/config.yml) for details via the corresponding dev libs.
* Python 3.8 with Python 3 PyGObject, posix-ipc, numpy, psutil and pyalsaaudio packages for the Side Bar
* The applications for the default keybindings are recommended in the debian setup and are xterm, firefox and alsa-utils (amixer). The default application key bindings are
  * Lin+T -> xterm
  * Lin+B -> firefox (B for browser)
  * Audio-keys -> amixer ... (mute toggle, louder, quieter)

## Credits
Original author: [Andreas Tennert](https://github.com/atennert)<br>
Current author: [Andreas Tennert](https://github.com/atennert)
