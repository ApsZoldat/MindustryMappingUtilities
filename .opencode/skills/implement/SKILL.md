---
name: Implement
description: Implement a ready-to-implement task from ai-artifacts/tasks/ and hand it off to review. Load when asked to implement a task, start the next task, or "do the task X".
---

# Implementing a task

## 1. Pick and preflight (main session)

- Task from the argument, else offer the rows currently in `ready-to-implement`.
- First re-check `blocked` rows: all deps `done` (or `cancelled`, noted) -> flip to
  `ready-to-implement`; rewrite the table (one whole-table edit).
- The chosen task must be `ready-to-implement` with deps `done` - otherwise stop and say
  why. Never start from `inbox` (unspecified) or `needs-info`.
- Read `task.md` + `notes.md` (create `notes.md` if missing), then the docs and source
  they cite under **Refs** - that list is the working context; rely on it first. Other
  docs (AGENTS.md file map, index docs) only if a step needs something Refs does not
  cover. Warn if a cited doc's `verified:` marker is behind `../Mindustry` HEAD -
  offer to run `sync-upstream` first.
- **Baseline compile:** `./gradlew build` must pass before touching anything. This is
  the desktop-only gate - the full `deploy` runs in CI and the user checks it. Baseline
  fails -> stop and report; no code.
- State -> `in-progress`.

## 2. Map (subagent)

`explore` subagent: touch points and usages in `../Mindustry` + `java/mu` for the task's
scope; returns file/symbol refs only. Keeps the big tree out of main context.

## 3. Code (main session)

The main agent writes the patch. Subagents **read**, they do not write here: fresh
context loses AGENTS.md style rules, and their diff would need verification anyway. If a
task genuinely has independent chunks, a writing subagent is allowed only with the style
rules + task constraints restated in its prompt, and you review its diff yourself.

Work the plan steps against the acceptance criteria.

## 4. Compile, hand off, stop

- `./gradlew build` green (same as baseline).
- `notes.md`: what changed, the **changed-files list** (this is review's diff scope -
  without it the uncommitted repo diff is attributable to nobody), and the **human test
  checklist** - edge cases you saw while writing, which `test` needs and cannot re-derive
  later.
- State -> `needs-review`.
- **STOP.** Tell the user: run `@review <task>`. Do not self-review, do not test, do not
  commit - review is a real gate (and so is the user's own `git diff`).

## Stalls

- Missing information -> `needs-info` + the question for the user.
- Discovered dependency on unfinished work -> `blocked` + `Depends on` entry.
- Scope change -> ask before inventing work.
