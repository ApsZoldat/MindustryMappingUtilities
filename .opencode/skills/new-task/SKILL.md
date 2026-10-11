---
name: new-task
description: Interactive task specification for the mod project. Interviews the user in batches of questions, reads project docs and engine sources, then writes ai-tasks/<task-name>/ with task.md, docs.md, api.md, checklist.md. Run BEFORE /implement-task. Never writes production code.
disable-model-invocation: true
---

# new-task

Turn a rough request into a self-contained task package that a weaker executor model can implement without guessing.

You write files ONLY under `ai-tasks/<task-name>/`. Never modify source code, docs/, or AGENTS.md.

## Principles

- Facts come from code and docs. Decisions come from the user. Never fill a gap with a guess: ask, or mark `[assumption]`.
- Never write an engine/library signature from memory. Every external symbol must be found in sources (grep) and recorded with `path:line`.
- Do not ask what you can find out yourself. State what you found instead. Exception: being unable to find something out after 2 attempts.
- Be critical. Point out contradictions with docs, code or earlier decisions, and flag vague or unmeasurable requirements. No praise, no filler, no preamble.
- Talk to the user in the user's language. Write all files in English.
- Files are later edited by a stronger model and read by a weak executor: terse, no references to the chat, no information duplicated between files. IDs (R1, AC1, A1, C1) and table structure are stable: never rename or renumber existing IDs. A removed item keeps its ID and is marked `[dropped]` with a reason.

## Tags

- `[decision]` the user decided this.
- `[fact]` verified in code or docs; cite the source.
- `[assumption]` not confirmed by the user; must also be listed under Open questions.
- `[proposed]` your suggestion, not yet confirmed. Must become `[decision]` or be dropped before finishing.

## Phases

A run has two sequential phases. The Status line in task.md tracks them.

| Status | Meaning |
|---|---|
| `interviewing` | Phase 1: requirements |
| `requirements-frozen` | Phase 1 done |
| `designing-api` | Phase 2: API design |
| `ready` | Package complete |

User commands (examples, accept equivalents in any language):

- "next", "design", "дальше": move from Phase 1 to Phase 2 (after the Step 3 checkpoint).
- "stop", "generate", "хватит", "генерируй": finish. In Phase 2 run Step 5. In Phase 1 write only task.md and checklist.md, set Status `requirements-frozen`, and state that docs.md and api.md were not created.

There is NO round limit. Never move to the next phase or finish on your own. Once per phase you may say in one line that you consider all branches resolved and list the remaining assumptions, then keep asking until the user gives a command.

## Step 0: Setup

1. Read AGENTS.md. List `ai-docs/` and read only the docs relevant to the task. `ai-docs/` holds conceptual docs only (for example Arc vs libgdx differences, editor principles) and no API signatures.
2. Take the initial description from the arguments or the user's message. If there is none, ask for it in one question.
3. Derive a kebab-case task name and confirm it in the first batch.
4. If `ai-tasks/<name>/` already exists, read all its files and resume from the phase given by Status. Never overwrite.
5. Create `ai-tasks/<name>/task.md` right away from the template (Status: interviewing).

## Step 1: Initial research (before the first questions)

- Find relevant docs in `ai-docs/`, mod sources and engine sources (`../Mindustry`), only as deep as needed to ask informed questions. The symbol-level research happens in Phase 2.
- If a read-only exploration subagent is available (`@explore`), delegate the exploration to it. Keep raw search output out of the main context.
- Build three internal lists: facts, open decisions, conflicts found.
- Record the Mindustry commit if git is readable: `git -C ../Mindustry rev-parse --short HEAD`. Otherwise write `unknown`.

## Step 2: Batch rules (both phases)

- 3 to 5 questions per batch, all from one area. Resolve parent decisions before child ones.
- Before the questions, at most 3 short lines of relevant facts you found (with paths). Nothing else as a preface.
- Format of a question: number, the question, then `Recommended:` your answer with a one-line reason. For closed choices list options A/B/C and mark the recommended one.
- Vague answers ("as usual", "nice", "fast"): ask for a measurable criterion.
- If an answer conflicts with a fact or an earlier decision, say so first, then ask which one wins.
- After EACH batch, write the new `[decision]`, `[fact]`, `[assumption]` items to disk. Do not rely on chat context to remember them.
- Check the facts you cite before you write them to disk.
- One run produces exactly one package. If the scope grows beyond what one executor session can hold (rough heuristic: more than a few new classes or more than one or two screens), say so in one line, name the parts that could be separate tasks, and ask the user to narrow the scope. Do not split it into several packages yourself.

## Step 3: Phase 1, requirements

Areas, in this order: goal and scope, user-visible behavior and UI, acceptance criteria, edge cases and failure behavior, manual verification.

Rules:

- Do not design classes here. If the user mentions class structure, record it under `## Design notes` in task.md as `[decision]`, without further discussion, and continue.
- Write acceptance criteria as soon as the behavior is clear. Update them when later answers change them.
- Once criteria exist, create `checklist.md` (skeleton below) and keep it in sync.

Checkpoint, run when the user says "next":

1. Feasibility: for every requirement that depends on an engine or mod capability, check that the capability exists (delegate to `@explore` if available). Report a table: `R | capability | feasible / unknown / conflict | path:line`. Conflicts and unknowns go back to the user. Stay in Phase 1 until each is resolved or dropped.
2. Coverage: every R has at least one AC, and every AC has at least one checklist row. Fix gaps.
3. Apply the scope rule from Step 2.
4. Set Status `requirements-frozen`, then continue straight into Phase 2.

## Step 4: Phase 2, API design

1. Set Status `designing-api`. Re-read task.md. Turn each Design note into a starting point for api.md and then remove the Design notes section.
2. Symbol research: for every engine or mod symbol the acceptance criteria need, find it in the sources (delegate to `@explore` if available; ask it to return `symbol | signature | path:line`). Build the verified table of docs.md progressively.
3. Interview areas, in this order: data and state, structure of new classes, integration with existing and engine code, remaining edge cases at class level.

### Designing api.md with the user

The user defines the API of the NEW mod classes. You may propose a structure (mark it `[proposed]`), but class names, responsibilities, fields and method signatures are the user's decisions. Discuss down to method signatures. Do not discuss method bodies: the executor writes those.

For each public member, elicit its contract: who calls it, on which thread, null/empty handling, side effects, errors.

Write the api.md draft to disk after each class-structure batch.

### Changes to frozen requirements

If Phase 2 shows that a requirement or criterion is infeasible, ambiguous or contradictory:

1. Say so, and name the affected R, AC and api.md members.
2. Ask the user for a decision.
3. After the answer, update task.md, checklist.md and api.md in the same turn. Mark changed or removed items (`[dropped]` keeps the ID), and add a Decisions log row noting `reopened in phase 2`. Status stays `designing-api`.

### Coverage check

Run after the class-structure block and again in Step 6: every AC maps to at least one api.md member, and every public member serves at least one AC or has a stated reason.

## Step 5: Generate the package (Phase 2, after the user stops)

Complete the four files in `ai-tasks/<name>/`. task.md and checklist.md already exist from Phase 1: finish them. Create docs.md and api.md from the drafts and the verified symbol table.

### task.md

```
# Task: <name>
Status: interviewing | requirements-frozen | designing-api | ready
Mindustry commit: <hash|unknown>

## Goal
1-3 sentences.

## Non-goals
- ...

## Requirements
- R1 [decision] ...

## Acceptance criteria
- AC1 (R1) Observable result that can be verified manually in the game.

## Design notes
(temporary, Phase 1 only; removed in Phase 2)

## Decisions log
| # | Question | Decision |

## Open questions and assumptions
- A1 [assumption] ...

## Deviations
(filled by /implement-task)
```

### docs.md

```
# Docs for <name>
Mindustry commit: <hash|unknown>

## Mindustry/Arc symbols used (verified)
| Symbol | Signature | Source path:line | Used for |

## ai-docs excerpts
### ai-docs/<file>
Only the parts needed for this task. Link to the doc for the rest. Conceptual material only: signatures belong to the verified table above.

## Constraints and pitfalls
- C1 threading, lifecycle, reflection and similar rules that apply to this task.

## Unverified
Must be empty when Status is ready. Otherwise list each symbol and the reason.
```

### api.md

New classes only. Signatures are complete and compilable. No method bodies.

````
# API of new classes: <name>

## Dependencies and creation order
- ClassA -> ClassB (uses)

## Classes
### src/path/File.java
Responsibility: one line.
```java
package ...;

import ...; // external types must exist in docs.md

public class X {
    /** Contract: caller, thread, null policy, side effects, errors. */
    public ReturnType method(ParamType p);
}
```

## Coverage
| AC | Members |
````

### checklist.md

Manual verification skeleton, derived from the acceptance criteria. Write expected results before any code exists. Steps stay high-level; /implement-task refines them with concrete UI paths.

```
| ID | AC | Preconditions | Steps (high-level) | Expected | Result |
```

Result stays empty (the user fills it). Besides the main scenario, add rows for: empty state, cancel or close, repeated open, window resize, errors in the log, and any other edge case from the interview.

## Step 6: Self-check

Fix every failure before reporting:

- Every R has at least one AC. Every AC has at least one checklist row and at least one api.md member (or an explicit reason why not).
- Every public api.md member serves at least one AC or has a stated reason.
- Every external type used in api.md appears in the verified table of docs.md.
- No `[proposed]` left. The Design notes section is gone. The Unverified section is empty, or the reason is stated and Status is not `ready`.
- No information duplicated between files. No filler sentences.
- Remaining assumptions appear in task.md under Open questions.
- Status is set to `ready` only if all checks pass.

## Step 7: Report

Reply in the user's language, briefly:

- list of created files;
- open assumptions;
- anything that blocks Status: ready;
- if the run ended in Phase 1, say that docs.md and api.md were not created.

No summary of the content of the files.