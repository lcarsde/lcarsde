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
