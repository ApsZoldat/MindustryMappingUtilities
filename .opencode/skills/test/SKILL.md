---
name: Test
description: Produce a human test protocol for a needs-testing task - environment, steps, expected results, edge cases. Load when asked to test a task, verify the last implementation, or when a task sits in needs-testing.
---

# Testing a task (human protocol)

There is no automated test infrastructure in this mod: no test source set, and CI runs
`./gradlew deploy` = build only. This skill produces a **protocol for the user** - the
user is the oracle, and never marks a task `done` from agent judgment alone. (Phase 2,
not built: a headless mod-load smoke test.)

## 1. Gather

- The task must be in `needs-testing` - otherwise report its actual state and stop.
- Read `task.md`, `notes.md` (implementation notes + the implementer's test checklist),
  and `git diff` restricted to the changed-files list.
- `mu-docs/Running.md` for build/install/launch steps.

## 2. Produce the protocol

Numbered, executable steps, each with an expected result:

1. **Environment** - exact build command, output jar, install location, how to launch,
   what to open (which map/dialog).
2. **Core checks** - one step per acceptance criterion.
3. **Edge cases** - from the checklist in `notes.md`, plus risk areas of the touched code
   (undo stack, save/reload, resize, reflection targets).
4. **Regression probes** - neighboring features the diff touched but did NOT aim at.
5. **CI** - after the user pushes, the Actions `deploy` job must be green (it is the only
   Android build).

## 3. Collect results

After the user reports the outcomes:

- all pass -> `done`
- failures -> back to `ready-to-implement`, failures appended to `notes.md`

One whole-table edit of `TaskStates.md`. Leave everything uncommitted.
