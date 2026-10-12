---
name: review-task
description: Reviews an implemented task package in ai-tasks/<task-name>/ against its spec (task.md, docs.md, api.md, checklist.md), fixes clear defects in the code itself, and writes review.md in the task folder. Run AFTER /implement-task, preferably in a fresh session.
disable-model-invocation: true
---

# review-task

Verify that the code matches the spec, fix clear defects with minimal changes, and report everything in `ai-tasks/<name>/review.md`.

You are not the implementer. Do not trust the implementation report or any earlier chat: derive every conclusion from the code and the package files.

## Principles

- Facts come from the package, the code and the sources. Never write or assert an engine/library signature or behavior from memory. A Mindustry/Arc-specific suspicion is a hypothesis: check it in `../Mindustry` (delegate to `@explore` if available) before acting on it.
- Every finding needs a `file:line`, evidence, and a spec reference (AC, api.md member, or constraint C). Findings without all three are dropped.
- Be critical, not exhaustive. Do not invent problems. If a pass finds nothing, say so.
- Minimal diff. Fix only what a finding requires. No refactoring, reformatting or renaming beyond it.
- No git write operations (commit, push, stash, reset, checkout, rebase). Read-only git commands are allowed.
- Talk to the user in the user's language. Code, comments and files are in English. No praise, no filler.

## Allowed writes

- Source files of the task: those listed in api.md and those changed by the implementation.
- `ai-tasks/<name>/review.md`.
- `ai-tasks/<name>/task.md`: Status, Deviations, Open questions only.
- `ai-tasks/<name>/api.md`: only to record a deviation you found, using the protocol from /implement-task (`D<n>` row, entry marked `[deviation D<n>]`).
- `ai-tasks/<name>/checklist.md`: Preconditions and Steps columns, and new rows. Never the Result column. Never remove rows or weaken Expected.

Everything else is read-only, including `ai-docs/` and AGENTS.md. Proposals for documentation go into review.md.

## Severity

| Level | Meaning |
|---|---|
| blocker | An acceptance criterion is not met, a crash, a build failure, a contract violation |
| major | A likely bug, a resource or listener leak, wrong thread, an unhandled error path |
| minor | Style, naming, small cleanup |
| note | Observation without required action |

## Fix policy

Fix directly (tier A) only if ALL hold:

- the finding is a clear violation of the spec or a contract, or a clear defect;
- the correct fix is certain and local (under 20 lines, one class);
- it changes no public signature and no user-visible behavior beyond making the code conform to the spec.

Also tier A: unused imports, leftover debug output, TODO or commented-out code.

Report only (tier B) when any of these holds:

- the fix needs a public signature change or a behavior the spec does not define;
- the fix is a design choice with real alternatives;
- the finding rests on an unverified engine hypothesis;
- the fix is large or spans several classes;
- you are not sure.

After each tier A fix: compile with `./gradlew jar`. If it fails, undo your change by hand. After 2 failed attempts, mark the finding unresolved (tier B) and move on.

Never fix anything by editing a requirement, acceptance criterion or `[decision]` entry.

## Step 0: Preconditions

1. The task name comes from the arguments. If missing, list `ai-tasks/` and ask.
2. Read `task.md`. If Status is `implemented`, continue. If it is `reviewing`, resume (see Step 7). Otherwise stop and tell the user to run /implement-task.
3. Tell the user once, in one line, that committing the implementation before the review keeps the review fixes separable in the diff. Do not wait for it.
4. Set Status `reviewing`.

## Step 1: Read

Read in this order: task.md (including Deviations and Open questions), api.md, docs.md, checklist.md. If review.md exists, read it too. Then read the code of every file listed in api.md and the files changed by the implementation (`git status` and `git diff --stat`, read-only).

## Step 2: Baseline build

Run `./gradlew jar`. A failing build is a blocker: find the cause and apply the fix policy.

## Step 3: Review passes

**P1 Traceability.** For every AC, find where the code realizes it and judge: met, partial, not met. Derive this from the code, not from the implementer's map.

**P2 API conformity.** Compare every member in api.md with the code: names, signatures, visibility, contracts (caller, thread, null policy, side effects, errors). Report mismatches. Unrecorded deviations are recorded by you in task.md Deviations as `D<n>` with the note `found by review`, and api.md is updated per the protocol.

**P3 Scope.** Compare the changed files with the allowed set. Unrelated edits (formatting, renames, other classes) are findings. Undo clearly accidental ones by hand.

**P4 Correctness and robustness.** Null and empty handling, boundaries, state transitions, error paths, resource and listener lifecycle (subscribe and unsubscribe, show and hide), threading rules from docs.md constraints, repeated invocation, re-entrancy. Use only rules written in docs.md and ai-docs, or verified in the sources.

**P5 UI behavior (by reading the code).** Layout and resize behavior, opening and closing repeatedly, focus and input handling, text overflow, user-visible strings and how the project localizes them, cancel and default paths. Rules for the engine's UI toolkit must come from docs or sources.

**P6 Code quality.** Dead code, duplication, magic numbers, naming, consistency with project conventions (from AGENTS.md and neighboring code), comments in English, warnings in the build output for changed files. Mostly minor.

**P7 Checklist.** Do the rows match the implemented UI (labels, paths, messages: grep the code for the exact strings)? Are negative and edge cases present? Add a regression row for each fixed defect. Do not weaken Expected. Then order all rows by risk: what is most likely broken, what the implementation touched last, what this review fixed, what depends on engine behavior you could not verify. Write the order to review.md.

**P8 Docs delta.** What from this task belongs in permanent documentation (`ai-docs/`)? Only propose it, in review.md.

## Step 4: Fix

Work through the findings by severity. Apply the fix policy one finding at a time, compiling after each.

## Step 5: Final build

Run `./gradlew jar` again. Re-check that no STUB markers remain: search for `STUB`. A failure here is a blocker.

## Step 6: Write review.md

If review.md exists, do not rewrite earlier rounds: add a new `## Round <n>` block at the end with the same sections, and update its Verdict.

```
# Review: <task-name>
Reviewed Status: implemented | Build: pass | fail | Mindustry commit: <hash|unknown>

## Verdict
reviewed | unresolved items remain

## Traceability
| AC | Code location | Verified how | Result (met / partial / not met) |

## API conformity
Mismatches only, then "all other members match".
| Member | Spec | Code | Result |

## Findings
| ID | Severity | File:line | Evidence | Spec ref | Action | Result |

## Fixes applied
### F<n>
Before: short snippet
After: short snippet
Why: one line
Verified: compile ok

## Unresolved (need user decision)
| ID | Problem | Options | Recommended |

## Checklist changes
- ...

## Manual verification order
| Order | Checklist ID | Why this position (risk) |

## Docs delta (proposal)
- ... belongs in ai-docs/<file>

## Not verified
What could not be checked (runtime and in-game behavior, anything needing the game).
```

The section "Not verified" is mandatory. This review reads code and compiles it; it does not run the game.

## Step 7: Status and report

- No unresolved blocker or major: Status `reviewed`.
- Otherwise leave Status `reviewing`. Re-running /review-task resumes: it re-checks the unresolved items and any code changed since.

Reply in the user's language, briefly:

- Verdict and build result;
- counts of findings by severity, how many were fixed;
- unresolved items that need the user's decision, each with a recommended answer;
- the first 3 rows of the Manual verification order, each with a one-line reason.

Do not summarize the code.