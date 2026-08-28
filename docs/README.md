# `docs/` — how the tracking docs fit together

These docs have two readers: the maintainer, and the coding agent that loads them
at the start of a session. Both want the same thing — the current plan, what's
left, and what already happened, with as little noise as possible. That's why the
files are split by job and capped by size: a lean working set is faster to scan
and cheaper to hold in context.

Five files, each with one job and one set of editors. Keeping them separate is what
stops parallel feature branches from colliding on the same lines.

| file | what it is | who edits it, and how |
|---|---|---|
| `roadmap.md` | **the plan** — phases, the tier→capability dependency spine, exit criteria | only when the plan itself changes. Feature branches must not touch it. |
| `duster-v1-tasks.md` | **the checklist** — one box per work item, grouped by phase | feature branches: flip `[ ]`→`[x]` **in place**, optional ` — PR #N` suffix. No moving, rewording, or regrouping. |
| `CHANGELOG.md` | **the history** — one dated line per change that landed on `master` | feature branches: **append one line at the end**. Never edit an existing line. |
| `duster-v1-design.md` | **the why** — numbered decision log (`#1`, `#2`, …) | when a decision is made: append a new numbered section. Numbers are permanent — other docs cite them. |
| `automation-tests-plan.md` | test strategy | when the test plan changes. |

`CHANGELOG.md` (and `archive/CHANGELOG-archive.md`) are marked `merge=union` in
`/.gitattributes`, so two branches that each append their own line merge with no
conflict — which holds only as long as nobody edits or reorders existing lines.

---

## Size guardrail

`.github/scripts/doc-size.sh` prints every file's line count and fails if one is
over its limit. CI runs it on any PR that touches `docs/`
(`.github/workflows/docs.yaml`).

| file | limit |
|---|---|
| `roadmap.md` | 400 |
| `duster-v1-tasks.md` | 500 |
| `duster-v1-design.md` | 500 |
| `CHANGELOG.md` | 500 |
| `automation-tests-plan.md` | 400 |

**When a file goes over, archive — do not raise the limit.**

## Archiving

Archived material moves under `docs/archive/`, keeping the original format so it
stays greppable. Do each roll on its own branch and merge it before other
doc-touching branches, so nobody rebases across the cut.

### `CHANGELOG.md` — scripted

```
.github/scripts/archive-changelog.sh            # keep newest 60 entries, roll the rest
.github/scripts/archive-changelog.sh --keep 100
.github/scripts/archive-changelog.sh --dry-run
```

Moves the oldest entries into `archive/CHANGELOG-archive.md` (append-only, same
newest-at-end order) and leaves a pointer comment in `CHANGELOG.md`.

### `duster-v1-design.md` — manual

Decisions are cited by number across the other docs, so numbers must not move.
When a decision is fully shipped and settled, replace its body with a one-line
stub and move the full text to `archive/duster-v1-design-archive.md`:

```
### 4. Session Ownership

Shipped. Full text: archive/duster-v1-design-archive.md#4-session-ownership
```

### `duster-v1-tasks.md` — manual

The `## Already shipped (pre–Phase 0)` appendix is the growth area. When the file
is over the limit, move that whole appendix to
`archive/duster-v1-tasks-shipped.md` and leave a one-line pointer. Completed
phases can follow the same way once every box in them is checked and released.

### `roadmap.md` / `automation-tests-plan.md`

These shouldn't grow much; going over the limit means the content drifted from
"plan" toward "status". Trim it back rather than archiving.
