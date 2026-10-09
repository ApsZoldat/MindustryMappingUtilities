---
name: New docs
description: Create documentation in ai-artifacts/ - term explainers, guides, class tree entries. Load when the user asks to document a class/system/task area, write a doc about X, or fill a docs folder.
---

# Creating docs from scratch

Workflow for producing **new** `ai-artifacts/` docs. The writing rules belong to the
`index-docs` skill - load it and follow them; this skill decides *what* to create and in
what order. `AGENTS.md` decides *whether* you may write at all.

Paths are relative to the repository root.

## 1. Scope

- **Which folder:** `arc-docs/` for Arc source, `mindustry-docs/` for Mindustry core,
  `mu-docs/` for this mod's own code. Pick by where the code you would patch lives;
  cross-link the other side when a finding spans both.
- **Which artifact:**
  - term explainer `<Term>.md` - a class/system you keep needing to explain
  - guide `Topic.md` - task-keyed "read before doing X" background
  - class tree entry - only curation/relationships, added inside the existing `XIndex.md`
- **Check what already exists** (file map in `AGENTS.md` + the folder's index) - one
  concept, one file. Never create a stub: the file and its rows appear together, with
  content.

## 2. The bar: re-derivation cost x staleness risk

Write only when the knowledge is **both**:

- **expensive to re-derive** - scattered across files, needs experimentation, easy to
  get subtly wrong, non-obvious cross-file relationships; **and**
- **stable enough not to rot** - not a signature list, not something one `read` reveals.

Method signatures and obvious structure stay in source: a doc duplicating them is worse
than no doc - it rots, and a trusted-but-stale doc makes readers skip the source that
would have corrected it. When unsure a fact clears the bar, ask the user before writing it.

## 3. Research first (subagents welcome)

Dispatch `explore` subagents to read the relevant source in parallel - one per area -
returning findings with file/symbol references. Every claim must come from source read
in this session (`index-docs` rule 1); subagent findings count only after you verify the
paths they cite. Subagents research; **the main agent writes the files**.

## 4. Write, then index - together

Per file, as one unit: create the file (with its `verified:` marker and relative links),
add its `XIndex.md` row, add its `AGENTS.md` file-map line. Never one without the others -
an unlisted file is invisible forever, a row without a file is a lie.

## 5. Check in and stop

Before creating more than ~3 new files in one go (`index-docs` rule 6), show the user
the plan first. Leave everything uncommitted for review (`index-docs` rule 8).
