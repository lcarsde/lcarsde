# lcarsde

lcarsde is a desktop environment for X11 with an LCARS-like look: a window manager plus Tool Apps, all in Kotlin/Native (Kotlin Multiplatform, `linuxX64` target), in one Gradle build.

Use the vocabulary in [`CONTEXT.md`](CONTEXT.md) (Window Manager, Tool App, Tool Window, Screen, Slot, …) in code, issues and docs. Prefer "lcarsde" over "LCARS" in names.

## Modules

- `lcarswm`: the Window Manager. Entry point `lcarswm/src/linuxX64Main/kotlin/lcarswm.kt`.
- `lcarsde-common`: GTK-free code shared by all modules (Tool App Protocol transport, logging, processes, files, Rx, DI).
- `lcarsde-gtk`: GTK binding for Tool Apps; becoming the lcarsde window toolkit (`lcarsde-window-toolkit`).
- `menu`: the Side Bar Tool App (still named "app menu" in code).
- `status-bar`: the Status Bar Tool App.
- `app-selector`: the App Selector Tool App.
- `logout`: the Logout Tool App.

Module responsibilities: [Role of the shared libraries](docs/adr/adrs/role-of-shared-libraries.md). Several code names lag behind `CONTEXT.md`; follow `CONTEXT.md` for new code.

## Build and test

Needs JDK 17 and the native dev libraries listed in `.github/workflows/` (libx11, libgtk-3, libxrandr, libpango, libcairo, libxml2, libpulse, …).

- `./gradlew build`: build everything.
- `./gradlew allTests`: run all tests (what CI runs).
- `./gradlew :lcarswm:allTests`: tests of one module.

Tests exist for `lcarswm` and `lcarsde-common` only.

`./run-xephyr.sh` runs the Window Manager and all Tool Apps from the debug build in a nested Xephyr window for manual testing (needs `xserver-xephyr` and `dex`). It uses an isolated session in `build/xephyr-session/`; the Window Manager log is in `build/xephyr-session/cache/lcarsde/lcarswm.log`. `./gradlew runXephyr` does the same through Gradle (`-PxephyrSize=WxH` sets the screen size).

## Read more

- [`CONTEXT.md`](CONTEXT.md): domain glossary.
- [`docs/adr/adr-index.md`](docs/adr/adr-index.md): architecture decisions and which modules they affect.
- [`docs/arc42/`](docs/arc42/): architecture documentation. Partly outdated; treat it as background, not truth.

## Agent skills

### Issue tracker

Issues live as local markdown files under `.scratch/<feature>/` (gitignored). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `CONTEXT.md` plus the existing ADRs in `docs/adr/`. See `docs/agents/domain.md`.

## Modules

Terms follow `CONTEXT.md`.

- `lcarsde-common`: shared non-GUI code for the Window Manager and Tool Apps, e.g. the Tool App Protocol message queues.
- `lcarsde-window-toolkit`: GTK binding and shared UI code for Tool Apps.
- `lcarswm`: the Window Manager; owns the X display, manages Application Windows and draws the lcarsde Layout on the Screens.
- `status-bar`: the Status Bar Tool App, shown in the Status Bar Slot.
- `side-bar`: the Side Bar Tool App, shown in the Side Bar Slot; lists the open Application Windows.
- `app-selector`: the App Selector Tool App for launching Applications.
- `logout`: the Logout Tool App for ending the session.
