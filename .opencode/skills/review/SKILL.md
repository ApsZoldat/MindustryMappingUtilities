---
name: Review
description: Review a task - cold-reader spec audit before implementation, or code review at needs-review. Load when asked to review a task/spec/implementation, when a task sits in needs-review, or as the final step of create-tasks.
---

# Reviewing a task

You did not write what you are reviewing: dispatch a **read-only subagent with fresh
context** as the second opinion - always, in both modes - then verify each finding in
the main session before acting on it. Never take a subagent finding on trust.

## Mode

- state is `needs-review`, or the task's `notes.md` has an implementation section
  (changed files) -> **code mode**
- otherwise -> **spec mode**

## Spec mode (invoked by `create-tasks`, or standalone on a stale spec)

**Cold-reader test:** given ONLY the task folder + cited docs - no grilling transcript,
no session memory - could an agent implement this? Checklist:

- acceptance criteria specific and individually testable ("works correctly" fails)
- scope is one PR-sized concern; no out-of-scope creep
- cites docs by link, does not quote them (drift check)
- refs resolve: every linked doc/source path exists; referenced symbols exist on v9
  (grep/LSP spot-check)
- deps exist, no cycles; constraints from the session got captured

Findings: fix the spec directly when trivial (it is just a file); structural findings go
back to `create-tasks` for its final grilling round. **Spec mode never touches states.**

## Code mode (exit gate for `implement`)

Scope: `git diff` limited to the changed-files list in `notes.md` - nothing is committed,
so the repo-wide diff is not attributable to one task.

Checklist:

1. Walk the task's acceptance criteria against the diff, item by item.
2. AGENTS.md style lint: paren spacing, same-line braces, camelCase constants, wildcard
   imports, arc collections, no boxed types, no `java.util.function`/`awt`/`Objects`,
   no main-loop allocation, short names, single-line javadoc.
3. Reflection audit: every reflected private field/method name verified against
   `../Mindustry` source - the mod's silent-failure mode #1 (a renamed field returns
   null, not an error).
4. Side effects: docs now stale -> propose the edit, ask first; follow-up work discovered
   -> stub an `inbox` row.

Findings format: `severity (blocker|nit) - file:symbol - why - suggested fix`.

**Fix authority:** blockers and nits are fixed directly in the main session, then
recompile; design-level questions -> state `needs-info` + the question for the user.

## Exit (code mode only)

- all blockers fixed and `./gradlew build` green -> `needs-testing`
- rework too large / deferred -> back to `in-progress`, findings appended to `notes.md`
- needs a user decision -> `needs-info`

One whole-table edit of `TaskStates.md`.
