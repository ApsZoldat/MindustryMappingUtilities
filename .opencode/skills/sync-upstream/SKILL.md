---
name: Sync upstream
description: Compare Anuken/Mindustry v9 against the last synced commit, propose doc updates, and stub API-change tasks. Load when the user asks to update after upstream changes, sync with Mindustry, or "run an update".
---

# Syncing with upstream Mindustry

State file: `ai-artifacts/upstream.md` (repo, branch, sha, date) - the last upstream
commit whose changes were reviewed into the docs. Doc freshness: each doc's `verified:`
marker records when it was last checked and against what.

## 1. Diff

1. Read `upstream.md`. Missing/garbled -> bootstrap: record the current `../Mindustry`
   HEAD as the baseline sha and ask the user whether to backfill history or start from
   today.
2. `git -C ../Mindustry fetch origin v9` - on network failure fall back to the GitHub
   compare view (`webfetch https://github.com/Anuken/Mindustry/compare/<sha>...v9`).
   If both fail, stop and say so; never guess what changed.
3. `git -C ../Mindustry log --oneline <sha>..origin/v9` and
   `git diff --name-only <sha>..origin/v9`.

## 2. Filter: what actually matters

An upstream change is relevant iff it touches:

- a path cited in some doc's `verified: ... sources:` marker, or
- a path named in a class tree entry of an `XIndex.md`, or
- something `java/mu/` imports or reflects into - a compile-level API break, even when
  no doc cites it.

Everything else is noise from a ~1000-file codebase - ignore it. Path matching can run
in `explore` subagents in parallel; the main agent assembles the proposal.

## 3. One batched proposal

Present in **one** message: the commits, affected docs with the specific stale claims,
proposed doc changes, proposed `inbox` task stubs for code fixes, and open tasks the
change invalidates (flag them "stale vs upstream", never auto-cancel). Then **wait for a
single approval** - no per-file asks.

## 4. Apply (only after approval)

- Update the affected docs following `index-docs` rules; keep `verified:` markers current.
- Task stubs: create `tasks/<name>/task.md` with goal + upstream commit links + affected
  files (minimal - `create-tasks` will spec it properly later), plus an `inbox` row.
  Before creating, check existing live rows (any state except `cancelled`/`done`) and
  note it there instead of duplicating.
- Flag invalidated open tasks in their `Blocker / next` column
  ("stale vs upstream `<short-sha>`").
- Advance `upstream.md` to the diffed tip (sha + date). If the user deferred some doc
  updates, still advance - but each deferred item must exist as an `inbox` stub so
  nothing is silently lost.

## 5. Housekeeping

Main agent writes; one whole-table edit of `ai-artifacts/tasks/TaskStates.md`; new/changed
files keep their index rows + `AGENTS.md` map lines current; no commits.
