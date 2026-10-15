#!/bin/bash
#
# Run the lcarsde session (Window Manager and Tool Apps) from the build output
# in a nested Xephyr X server for manual testing. Nothing gets installed and the
# host's configuration and autostart entries are not used.
#
# Usage: ./run-xephyr.sh [--no-build] [--size WxH] [-- extra Xephyr args]
#
# The session lives in build/xephyr-session/. Config files are copied there from
# the module resources on every run; logs end up in build/xephyr-session/cache/lcarsde/
# and the console output of the session in build/xephyr-session/session.log.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SESSION="$ROOT/build/xephyr-session"
BUILD=1
SIZE="1280x800"
XEPHYR_EXTRA_ARGS=()

while [ $# -gt 0 ]; do
    case "$1" in
        --no-build) BUILD=0 ;;
        --size) SIZE="$2"; shift ;;
        --) shift; XEPHYR_EXTRA_ARGS=("$@"); break ;;
        -h|--help) sed -n '3,10p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "Unknown argument: $1" >&2; exit 1 ;;
    esac
    shift
done

missing=()
command -v Xephyr >/dev/null || missing+=("xserver-xephyr")
command -v dex >/dev/null || missing+=("dex")
if [ ${#missing[@]} -gt 0 ]; then
    echo "Missing tools. Install them with: sudo apt install ${missing[*]}" >&2
    exit 1
fi
if [ -z "${DISPLAY:-}" ]; then
    echo "DISPLAY is not set; Xephyr needs a running X (or XWayland) session." >&2
    exit 1
fi

# --- build ------------------------------------------------------------------

if [ "$BUILD" = 1 ]; then
    "$ROOT/gradlew" -p "$ROOT" \
        :lcarswm:linkDebugExecutableLinuxX64 \
        :status-bar:linkLcarsde-status-barDebugExecutableLinuxX64 \
        :side-bar:linkLcarsde-side-barDebugExecutableLinuxX64 \
        :app-selector:linkLcarsde-app-selectorDebugExecutableLinuxX64 \
        :logout:linkLcarsde-logoutDebugExecutableLinuxX64
fi

# --- session directory --------------------------------------------------------

rm -rf "$SESSION/bin" "$SESSION/share" "$SESSION/autostart" "$SESSION/config"
mkdir -p "$SESSION/bin" "$SESSION/share/lcarsde" "$SESSION/autostart" \
    "$SESSION/config/lcarsde" "$SESSION/cache/lcarsde" "$SESSION/data/applications"

link_binary() { # module, executable dir
    local exe="$ROOT/$1/build/bin/linuxX64/$2"
    local kexe
    kexe="$(find "$exe" -maxdepth 1 -name '*.kexe' 2>/dev/null | head -n 1)"
    if [ -z "$kexe" ]; then
        echo "No executable in $exe; run without --no-build." >&2
        exit 1
    fi
    ln -s "$kexe" "$SESSION/bin/$(basename "$kexe")"
}
link_binary lcarswm debugExecutable
link_binary status-bar lcarsde-status-barDebugExecutable
link_binary side-bar lcarsde-side-barDebugExecutable
link_binary app-selector lcarsde-app-selectorDebugExecutable
link_binary logout lcarsde-logoutDebugExecutable

for module in lcarswm status-bar side-bar app-selector logout; do
    resources="$ROOT/$module/src/linuxX64Main/resources"

    # styles: Tool Apps read them via LCARSDE_DATA_DIR
    if [ -d "$resources/usr/share/lcarsde" ]; then
        for dir in "$resources/usr/share/lcarsde"/*; do
            ln -s "$dir" "$SESSION/share/lcarsde/$(basename "$dir")"
        done
    fi

    # config: user config takes precedence over /etc in the Window Manager and Status Bar
    if [ -d "$resources/etc/lcarsde" ]; then
        cp "$resources/etc/lcarsde"/* "$SESSION/config/lcarsde/"
    fi

    # autostart entries, pointed at the build output
    if [ -d "$resources/etc/xdg/autostart" ]; then
        for desktop in "$resources/etc/xdg/autostart"/*.desktop; do
            sed "s#^Exec=/usr/bin/#Exec=$SESSION/bin/#" "$desktop" \
                > "$SESSION/autostart/$(basename "$desktop")"
        done
    fi
done

# --- X server -----------------------------------------------------------------

display=1
while [ -e "/tmp/.X11-unix/X$display" ] || [ -e "/tmp/.X$display-lock" ]; do
    display=$((display + 1))
done

Xephyr ":$display" -screen "$SIZE" -resizeable -ac -br -noreset "${XEPHYR_EXTRA_ARGS[@]}" \
    > "$SESSION/xephyr.log" 2>&1 &
xephyr_pid=$!
wm_pid=""
tail_pid=""

cleanup() {
    if [ -n "$wm_pid" ]; then
        # stop the Window Manager while Xephyr is still running, it only handles SIGTERM with a live X server
        local pids pid alive
        pids="$(pgrep -P "$wm_pid" || true) $wm_pid"
        for pid in $pids; do kill -TERM "$pid" 2>/dev/null || true; done
        for _ in $(seq 30); do
            alive=0
            for pid in $pids; do
                if kill -0 "$pid" 2>/dev/null; then alive=1; fi
            done
            [ "$alive" = 0 ] && break
            sleep 0.1
        done
        for pid in $pids; do kill -KILL "$pid" 2>/dev/null || true; done
        wait "$wm_pid" 2>/dev/null || true
        wm_pid=""
    fi
    if [ -n "$tail_pid" ]; then
        kill "$tail_pid" 2>/dev/null || true
        wait "$tail_pid" 2>/dev/null || true
        tail_pid=""
    fi
    kill "$xephyr_pid" 2>/dev/null || true
    wait "$xephyr_pid" 2>/dev/null || true
}
trap cleanup EXIT
trap 'cleanup; exit 130' INT
trap 'cleanup; exit 143' TERM

for _ in $(seq 50); do
    [ -e "/tmp/.X11-unix/X$display" ] && break
    if ! kill -0 "$xephyr_pid" 2>/dev/null; then
        echo "Xephyr failed to start, see $SESSION/xephyr.log" >&2
        exit 1
    fi
    sleep 0.1
done

# --- session ------------------------------------------------------------------

export DISPLAY=":$display"
export PATH="$SESSION/bin:$PATH"
export XDG_CONFIG_HOME="$SESSION/config"
export XDG_CACHE_HOME="$SESSION/cache"
export XDG_DATA_HOME="$SESSION/data"
export XDG_CURRENT_DESKTOP="LCARSWM"
export XDG_MENU_PREFIX="lcarswm-"
export LCARSWM_AUTOSTART_DIR="$SESSION/autostart"
export LCARSDE_DATA_DIR="$SESSION/share/lcarsde"

echo "lcarsde is running on DISPLAY=$DISPLAY"
echo "Window Manager log: $XDG_CACHE_HOME/lcarsde/lcarswm.log"
echo "Session output: $SESSION/session.log"
echo "Quit with Lin+Q in the Xephyr window or Ctrl+C here."

run=(lcarswm.kexe)
if command -v dbus-run-session >/dev/null; then
    run=(dbus-run-session -- lcarswm.kexe)
fi
# run in the background and wait, so that the traps fire immediately on SIGTERM (e.g. from Gradle)
# the output goes to a file, so a closed console pipe cannot SIGPIPE the Window Manager
: > "$SESSION/session.log"
"${run[@]}" > "$SESSION/session.log" 2>&1 &
wm_pid=$!
tail -n +1 -F --pid="$wm_pid" "$SESSION/session.log" &
tail_pid=$!
wm_status=0
wait "$wm_pid" || wm_status=$?
wm_pid=""

# closing the Xephyr window ends the session normally, even though lcarswm exits with an error then
if ! kill -0 "$xephyr_pid" 2>/dev/null; then
    wm_status=0
fi
exit "$wm_status"
