---
name: Create tasks
description: Grill the user to compile docs and preferences into implementation-ready task folders in ai-artifacts/tasks/, with dependencies. Load when the user wants to plan, specify, or break work down into tasks.
---

# Creating tasks by grilling

Output: `ai-artifacts/tasks/<name>/task.md` folders (implementation-ready specs) plus rows
in `ai-artifacts/tasks/TaskStates.md`. Downstream skills read the task folder, **not** the
docs - the folder must carry everything needed to implement, by *citing* docs, never by
copying them (copied text rots with no maintainer).

## 1. Facts first, decisions to the user

Read: `TaskStates.md` (existing work, blocked rows), the relevant index rows and docs.
Dispatch `explore` subagents for any fact findable in the repo/source - **never ask the
user something the filesystem can answer**. Only decisions go to the user.

## 2. Grilling rounds

Work a design tree in rounds. The **frontier** = questions answerable right now without
guessing at answers you haven't heard yet. Ask the whole frontier in one round, numbered,
each with a recommended answer:

```
- **Q1 - <title>**: <body, might be multiple paragraphs, including choices>

> <your recommended answer>
```

Wait for the answers; recompute the frontier; next round. Word each question so "yes"
accepts your recommendation. A question that depends on an open one belongs to a later
round. Done only when the frontier is empty **and** the user confirms shared understanding.

API surface is part of the frontier: for every class a task creates or modifies, agree its
fields and method signatures with the user before it leaves grilling (arc collections, no
boxing, public fields over getters/setters - per AGENTS.md). New/changed signatures are
decisions, not facts, and go into `task.md` as spec; `/implement` codes to the agreed
surface instead of inventing its own. Untouched existing APIs stay `file` + symbol
citations, never restated - that's rule 1's "the filesystem can answer" territory.

## 3. Compile into task folders

- One task = one independently verifiable, PR-sized concern. Dependencies go in the
  `Depends on` column (`TaskStates.md`), depth <= ~3, no cycles - check before writing.
- `task.md` sections: **Goal** / **Scope** (files to touch) / **Plan steps** (carries the
  agreed new/changed class surfaces - signatures, not bodies) /
  **Acceptance criteria** (specific, individually testable - "works correctly" fails) /
  **Constraints** (decisions from this session) / **Refs** (links to docs + source,
  cited never quoted) / **Open questions**.
- Repo-wide preferences -> propose promoting them to `AGENTS.md` (user decides);
  task-specific ones -> `Constraints`.

## 4. Spec review, then states

Run the `review` skill (load it, id `review`) in spec mode - but as a **parallel set of
distinct lenses**, not one reviewer. Per task, dispatch all of these read-only subagents
at once (`background: true`, fresh context each, only the task folder + cited docs - no
grilling transcript):

1. **Testability lens** - acceptance criteria specific and individually testable
   ("works correctly" fails); scope is one PR-sized concern; constraints from the
   session got captured.
2. **Drift lens** - cites docs by link, never quotes them; every ref path exists;
   referenced symbols exist on `v9` (grep/LSP spot-check); deps exist, no cycles.
3. **Cold-reader lens** - the pure test: given only the folder + cited docs, could an
   agent implement this? Plan steps carry the agreed class surfaces (signatures, not
   bodies); no hidden session knowledge; open questions are actually open.

Merge the returns, drop duplicates, and **verify every finding in the main session**
before acting - never take a subagent finding on trust. Trivial fixes: fix the spec
directly. Structural findings: answer them in a final grilling round with the user.

Then write the row: `ready-to-implement`, or `needs-info` (open questions), or `blocked`
(deps not done). Main agent only, one whole-table edit of `TaskStates.md`.

## 5. Doc updates and commits

If the session surfaced doc changes, list them and **wait for explicit acceptance** before
writing any of them (follow `index-docs`). New/changed files keep their index rows +
`AGENTS.md` map lines current. Leave everything uncommitted.
