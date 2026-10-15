# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`CONTEXT.md`** at the repo root.
- **`docs/adr/adr-index.md`**: lists every ADR and which subprojects (lcarswm, menu, status-bar, app-selector) it
  applies to. Read the ADRs in `docs/adr/adrs/` that touch the area you're about to work in.
- **`docs/arc42/`**: arc42 architecture documentation (context, building blocks, runtime view, concepts). Consult the
  relevant chapter when you need architectural context.

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront.
The `/domain-modeling` skill (reached via `/grill-with-docs` and `/improve-codebase-architecture`) creates them lazily
when terms or decisions actually get resolved.

## File structure

Single-context repo:

```
/
├── CONTEXT.md
├── docs/
│   ├── adr/
│   │   ├── adr-index.md               ← ADR × subproject relevance table
│   │   ├── template.md                ← template for new ADRs
│   │   └── adrs/
│   │       ├── language-for-wm.md
│   │       └── ipc-between-wm-and-menu.md
│   └── arc42/
│       └── 12_glossary.md
├── lcarswm/
├── menu/
├── status-bar/
└── ...
```

## Recording new ADRs

- Use `docs/adr/template.md`.
- Name the file descriptively in kebab-case (e.g. `use-of-colors.md`), not with a numeric prefix.
- Place it in `docs/adr/adrs/` and add a row to the table in `docs/adr/adr-index.md`, marking the affected subprojects.

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the
term as defined in `CONTEXT.md` (or the arc42 glossary). Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal: either you're inventing language the project doesn't
use (reconsider) or there's a real gap (note it for `/domain-modeling`).

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> _Contradicts the "IPC between window manager and application menu" ADR, but worth reopening because…_
