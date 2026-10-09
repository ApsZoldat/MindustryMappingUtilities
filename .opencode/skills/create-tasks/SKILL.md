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
❓ **Q1 - <title>**: <body, might be multiple paragraphs, including choices>

➡️ <your recommended answer>
```

Wait for the answers; recompute the frontier; next round. Word each question so "yes"
accepts your recommendation. A question that depends on an open one belongs to a later
round. Done only when the frontier is empty **and** the user confirms shared understanding.

## 3. Compile into task folders

- One task = one independently verifiable, PR-sized concern. Dependencies go in the
  `Depends on` column (`TaskStates.md`), depth <= ~3, no cycles - check before writing.
- `task.md` sections: **Goal** / **Scope** (files to touch) / **Plan steps** /
  **Acceptance criteria** (specific, individually testable - "works correctly" fails) /
  **Constraints** (decisions from this session) / **Refs** (links to docs + source,
  cited never quoted) / **Open questions**.
- Repo-wide preferences -> propose promoting them to `AGENTS.md` (user decides);
  task-specific ones -> `Constraints`.

## 4. Spec review, then states

Run the `review` skill (load it, id `review`) in spec mode - a cold reader with only the
task folder + cited docs - and answer its findings in a final grilling round. Then write
the row: `ready-to-implement`, or `needs-info` (open questions), or `blocked` (deps not
done). Main agent only, one whole-table edit of `TaskStates.md`.

## 5. Doc updates and commits

If the session surfaced doc changes, list them and **wait for explicit acceptance** before
writing any of them (follow `index-docs`). New/changed files keep their index rows +
`AGENTS.md` map lines current. Leave everything uncommitted.
