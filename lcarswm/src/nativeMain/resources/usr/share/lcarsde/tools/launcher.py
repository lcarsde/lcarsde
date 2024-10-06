#!/usr/bin/env python3

import sys

from gi.repository import Gio


def launch(my_name, desktop):
    launcher = Gio.DesktopAppInfo.new_from_filename(desktop)
    launcher.launch([], None)


if __name__ == "__main__":
    launch(*sys.argv)
