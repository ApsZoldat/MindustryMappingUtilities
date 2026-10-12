---
name: implement-task
description: Implements a task package from ai-tasks/<task-name>/ (task.md, docs.md, api.md, checklist.md) made by /new-task. Builds a compiling skeleton from api.md first, then fills in the method bodies, records deviations, and refines the manual checklist. Run AFTER /new-task and BEFORE /review-task.
disable-model-invocation: true
---

# implement-task

Implement exactly what the task package specifies, so that the project compiles and the user can verify the result manually in the game.

The package is the contract. You do not redesign it, extend it, or "improve" it.

## Principles

- Facts come from the package and the sources. Never write an engine/library signature from memory: use the verified table in docs.md, or find the symbol in the sources (grep) first.
- Never change a public signature, add a public member, or add a file that is not in api.md without the deviation protocol below.
- Minimal diff. No refactoring, reformatting or renaming of code the task does not require. No new dependencies. No build configuration changes unless task.md says so.
- Stop instead of improvising when a stop condition below is met.
- No git write operations (commit, push, stash, reset, checkout, rebase). Read-only git commands are allowed.
- Talk to the user in the user's language. Code, comments and files are in English. No praise, no filler.

## Allowed writes

- Source files listed in api.md, plus existing source files that task.md or api.md explicitly says to modify.
- `ai-tasks/<name>/task.md`: Status, Deviations, Open questions only.
- `ai-tasks/<name>/api.md`: only through the deviation protocol.
- `ai-tasks/<name>/checklist.md`: Preconditions and Steps columns, and new rows. Never the Result column.
- `ai-tasks/<name>/docs.md`: only to refresh `path:line` of a symbol that still exists with the same signature.

Everything else is read-only: `ai-docs/`, AGENTS.md, build files, unrelated sources.

## Gates

Do not pass a gate until its condition holds.

| Gate | Condition |
|---|---|
| G1 | Preconditions ok (Step 0) |
| G2 | Engine symbols re-verified (Step 2) |
| G3 | Skeleton compiles and the user confirmed it (Step 3) |
| G4 | No STUB markers left and the project compiles (Step 4) |
| G5 | Spec conformity check passed (Step 5) |

## Step 0: Preconditions (G1)

1. The task name comes from the arguments. If missing, list `ai-tasks/` and ask which one.
2. Read `ai-tasks/<name>/task.md`. If Status is `ready`, continue. If Status is `implementing`, resume (remaining work = classes that still contain STUB markers). If it is anything else, stop and tell the user to finish the package with /new-task.
3. docs.md must have an empty Unverified section. Otherwise stop.
4. The build command is `./gradlew jar`, unless AGENTS.md specifies a different one. It only compiles and packages. Never launch the game.
5. Compare the Mindustry commit in docs.md with the current one: `git -C ../Mindustry rev-parse --short HEAD`. A mismatch means the verified table may be stale: do Step 2 in full and say so in the report.
6. Run a read-only `git status`. If files the task touches already have uncommitted changes, warn the user and ask whether to proceed.
7. Set Status `implementing`.

## Step 1: Read

Read in this order: task.md, api.md, docs.md, checklist.md. Read other files only when a task step needs them.

Build a work list internally: files to create or modify, in the order given by "Dependencies and creation order" in api.md.

## Step 2: Re-verify engine symbols (G2)

For every row of the verified table in docs.md, confirm the symbol still exists at the recorded path with the recorded signature. If a read-only exploration subagent (`@explore`) is available, delegate this. You may skip the check only if the commit matches and the working tree of `../Mindustry` is clean.

- Same signature, moved line: update `path:line` in docs.md.
- Signature changed or symbol missing: stop (stop condition S2).

## Step 3: Skeleton (G3)

1. Create every new class and member from api.md with its exact signature, package and imports. Copy the Javadoc contracts verbatim.
2. For members added to existing classes, do the same.
3. Every body is a stub: `throw new UnsupportedOperationException("STUB");` or the simplest default return value with a trailing `// STUB` comment. The marker must be greppable.
4. Compile.
5. Fix only skeleton errors (imports, typos, visibility). If an error shows that api.md itself is wrong (a type does not exist, a signature cannot compile), use the deviation protocol or stop.
6. PAUSE. Report briefly: created and modified files, the compile result, and any deviation the skeleton needed. Do not start Step 4 until the user explicitly confirms. On resume, if any class already has a real implementation (no STUB), treat the skeleton as confirmed and do not ask again.

## Step 4: Implement (G4)

Work through the work list in dependency order, one class at a time:

1. Implement the bodies. Honor every contract in api.md: caller, thread, null and empty policy, side effects, errors.
2. Remove the STUB marker as you replace a stub.
3. Compile after each class. Report errors condensed to the first few distinct ones.
4. Guard: if the same compile error survives 3 fix attempts, stop (S5).

When a detail is unspecified:

- If the guess would change user-visible behavior or an acceptance criterion, ask the user (S4).
- Otherwise choose the simplest option and record it in task.md under Open questions as `A<n> [assumption]`, with the member it concerns.

### Parallel mode (optional, only if the user asked for it)

Use only when all of these hold: the skeleton compiles, at least 3 classes are independent, each has substantial logic (roughly 100+ lines), and they share no mutable state.

- One subagent per class, and one file per subagent. Restrict its write access to that file if permissions allow.
- Subagents do not run the build. Only you compile.
- Give each subagent: the file path, the relevant api.md section, the AC IDs it serves, the relevant docs.md rows, and the rule to report instead of improvising.
- Subagents return a list of deviations and assumptions. You apply the protocol and compile.
- Do not split a single class across subagents.

## Deviation protocol

A deviation is any change to a public or package-private signature, a member added or removed, a file not listed in api.md, or behavior different from a contract. Private helpers, private fields and local constants are not deviations.

1. Decide whether it is forced by the compiler or the engine, and whether it leaves every acceptance criterion's observable behavior unchanged. If not, ask the user (S4). Do not decide for them.
2. Record it in task.md under Deviations: `D<n> | member or AC | spec said | implemented | reason`.
3. Update api.md to match the code and mark the entry `[deviation D<n>]`, so api.md stays the truth.
4. Never edit requirements, acceptance criteria or `[decision]` entries yourself.

## Stop conditions

Stop, leave Status `implementing`, and report. Do not improvise.

| Code | Condition |
|---|---|
| S1 | The spec contradicts itself or contradicts the sources |
| S2 | An engine symbol is missing or its signature changed |
| S3 | An acceptance criterion cannot be met with the specified API |
| S4 | A decision is needed that belongs to the user |
| S5 | The same compile error survives 3 attempts |
| S6 | A change outside the allowed writes is needed |

The report names the code, the affected IDs, and a recommended resolution.

## Step 5: Conformity check (G5)

1. Search for the STUB marker. Any hit means G4 is not met.
2. Every AC has a place in the code. Build a compact map `AC -> class.method`.
3. Every public member in api.md exists with the same signature, or is covered by a deviation entry.
4. Spot-check contracts: thread, null policy, error behavior, call order.
5. List the changed files and compare with the allowed set. Anything unexpected must be explained or undone by hand.
6. No leftover debug output, TODO comments, commented-out code, or unused imports.
7. Final full compile passes.

## Step 6: Refine checklist.md

For each row, replace the high-level Steps with concrete ones using the real UI as implemented:

- how to build (`./gradlew jar`) and how to launch the game with the built jar (from AGENTS.md);
- menu paths, button labels and dialog titles exactly as in the code;
- exact inputs and expected on-screen results;
- where to look in the log, with the exact message text if the code logs it.

Rules: do not remove rows, do not weaken Expected results, do not touch Result. Add rows for edge cases discovered while implementing.

## Step 7: Finish

Set Status `implemented` only if G4 and G5 hold and no stop condition is open. Otherwise leave `implementing`.

Reply in the user's language, briefly:

- Status and compile result;
- changed and created files;
- deviations (IDs and one-line reasons) and added assumptions;
- `AC -> code` map;
- the top 3 riskiest checklist rows to try first;
- blockers or stop codes, if any;
- next step: /review-task.

Do not summarize the code you wrote.