---
name: Index docs
description: Write or correct documentation in ai-artifacts/ - term explainers, class tree entries, guides, decision write-ups. Load before authoring, updating, or deleting any file under ai-artifacts/.
---

# Writing ai-artifacts docs

Rules for authoring docs in `ai-artifacts/`. Whether you may write at all is decided in
`AGENTS.md` ("How to work with ai-artifacts") - this skill covers *how*.

Paths below are relative to the repository root.

## 1. Read before you write

Every claim must come from source you read **in this session**. Prior knowledge of
Mindustry, Arc or libGDX is a hint about where to look, never a source: training data
predates the `v9` branch, so recalled API details are likely wrong. If you did not read
it, do not write it.

When unsure, say so in the file rather than smoothing it over.

## 2. Write for a reader who knows libGDX, not Arc

Assume the reader is fluent in Java, libGDX and scene2d and knows nothing about
Arc/Mindustry specifics. **Explain the delta, not the category.**

Don't spend lines on what a `Table` or a dialog *is*. Do cover how Arc's version differs
from the libGDX one, what is non-obvious, what breaks, and how it relates to what this
mod patches.

## 3. Shape and length

| File | Budget |
|---|---|
| Term explainer (`arc-docs/Element.md`) | ~30-80 lines |
| Guide (`arc-docs/UI.md`) | ~40-100 lines |
| Class tree entry (in the index) | ~100-150 lines total, per `AGENTS.md` |

Shorter beats padded. House philosophy: **if something is obvious - the class name
literally says what the class does, one `read` reveals it - it is removed; only
unobvious/undocumented knowledge remains.** A doc that restates the source duplicates
something that cannot rot and adds a second thing to keep in sync.

## 4. Mark every file as verifiable

First line of each doc:

```md
<!-- verified: v9 @ 2026-10-09, sources: ui/dialogs/CustomRulesDialog.java -->
```

Record the branch it was observed on. Update the marker whenever you re-verify. Cite
paths + symbol names, **never line numbers** - they rot immediately.

## 5. Naming, links, atomicity

- Term files: exact class/symbol name for classes (`Element.md`), CamelCase for concepts
  (`Rendering.md`). The `AGENTS.md` file-map entry must match the filename **exactly** -
  Linux is case-sensitive.
- Guide files: CamelCase topic (`UI.md`, `Coordinates.md`). One guide per topic.
- Adding an `AGENTS.md` file-map entry and creating its file happen **together**: no
  entries pointing at missing files, no files missing their entry (an unlisted file is
  invisible forever).
- One concept, one file. Check what exists before creating; check the file map first.
- Link between docs relatively (`../arc-docs/UI.md`) so they work on GitHub and locally.
- Findings spanning Arc and Mindustry: put the file where the code you would patch lives,
  cross-link the other side.
- Never copy `AGENTS.md` content (style rules, key decisions) into `ai-artifacts/` - two
  copies drift. Link to it instead.

## 6. Scope and volume

Curation applies to writing too: document subsystems **this mod actually touches**, not
every package in a 1000-file codebase. Irrelevant coverage is noise that makes the file
map less useful.

Before creating more than ~3 new files in one go, check in with the user first.

## 7. Staleness and deletion

If a doc contradicts source, the source wins - propose fixing **or deleting** it. A
removed doc is better than a wrong one: readers trust docs and skip the source that would
have corrected them. Surface the problem to the user rather than silently rewriting.

## 8. No commits

Leave changes uncommitted for the user to review.
