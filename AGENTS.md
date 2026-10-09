# AGENTS.md

Instructions for AI agents working on this repository.

## Basic context

- **[Mindustry](https://github.com/Anuken/Mindustry/tree/master)** - An automation tower defense RTS game written on Java.
    - Work specifically with [V9 branch](https://github.com/Anuken/Mindustry/tree/v9) of Mindustry
- **[Arc](https://github.com/Anuken/Arc)** - Mindustry's gamedev framework based off of libGDX.
    - Mindustry V9 has the entirety of Arc included in its repo, instead of being an external dependency.
- **[Mindustry Mapping Utilities](https://github.com/ApsZoldat/MindustryMappingUtilities)** - This mod for Mindustry, aims to improve map editor's convenience and possibilities.

### How does Arc differ from libGDX?

There are too many things to list, but here are some highlights:

- Soloud used as the audio engine across all platforms - faster, more consistent and more capable than libGDX's per-platform abstraction
- SDL used as the desktop backend library instead of GLFW - comes with its own benefits and drawbacks
- Removal of GWT module and all workarounds associated with it
- Proper methods for drawing lines, polygons, etc in one sprite batch
- Global sprite batch, texture atlas, asset manager, etc
- Thin GL abstraction layer, state is cached to prevent unnecessary API calls
- All APIs deal with 2D coordinates instead of attempting to share 2D and 3D classes (cameras, matrices, etc)
- 3D rendering abstractions mostly removed
- Simplification of most graphics abstractions (Framebuffer, Texture, Mesh, etc.)
- OpenGL ES 3.0 as a minimum target
- Java 8 target, heavy usage of lambdas in Scene2D code
- Massive amount of refactored, merged, deleted classes

---

## Code style

### Formatting guidelines

- No spaces around parentheses: `if(condition){`, `SomeType s = (SomeType)object`
- Same-line braces.
- 4 spaces indentation
- `camelCase`, **even for constants or enums**.
- No underscores for anything.
- Do not use braceless `if/else` statements. `if(x) statement else statement2` should **never** be done. In very specific situations, having braceless if-statements on one line is allowed: `if(cond) return;` would be valid.
- Prefer single-line javadoc `/** @return for example */` instead of multiline javadoc whenever possible
- Short method/variable names (multipleLongWords should be avoided if it's possible to do so reasonably, especially for variables)
- Use wildcard imports - `import some.package.*` - for everything. This makes incorrect class usage more obvious (*e.g. arc.util.Timer vs java.util.Timer*) and leads to cleaner-looking code.

### Do not use incompatible Java features (java.util.function, java.awt, java.lang.Objects).
Android and RoboVM (iOS) do not support many of Java 8's features, such as the packages `java.util.function`, `java.util.stream` or `forEach` in collections. Do not use these in your code.
If you need to use functional interfaces, use the ones in `arc.func`, which are more or less the same with different naming schemes.

The same applies to any class *outside* of the standard `java.[n]io` / `java.net` / `java.util` packages: Most of them are not supported.
`java.awt` is one of these packages: do not use it, ever. It is not supported on any platform, even desktop - the entire package is removed during JRE minimization.
In general, if you are using IntelliJ, you should be warned about platform incompatiblities.

> **Note for this mod:** these rules are written for the game's multiplatform constraints.
> The v9 target is Java 17, but keep following them (especially `arc.func` instead of
> `java.util.function`, and no `java.awt`) so the code stays consistent with the Mindustry/Arc
> codebase it patches into.

### Use `arc` collections and classes when possible.
Instead of using `java.util.List`, `java.util.HashMap`, and other standard Java collections, use `Seq`, `ObjectMap` and other equivalents from `arc.struct`.
Why? Because that's what the rest of the codebase uses, and the standard collections have a lot of cruft and usability issues associated with them.
In the rare case that concurrency is required, you may use the standard Java classes for that purpose (e.g. `CopyOnWriteArrayList`).

What you'll usually need to change:
- `HashSet` -> `ObjectSet`
- `HashMap` -> `ObjectMap`
- `List` / `ArrayList` / `Stack` -> `Seq`
- `java.util.Queue` -> `arc.struct.Queue`
- *Many others*

### Avoid boxed types (Integer, Boolean)
Never create variables or collections with boxed types `Seq<Integer>` or `ObjectMap<Integer, ...>`. Use the collections specialized for this task, e.g. `IntSeq` and `IntMap`.

### Do not allocate anything if possible.
Never allocate `new` objects in the main loop. If you absolutely require new objects, use `Pools` to obtain and free object instances.
Otherwise, use the `Tmp` variables for things like vector/shape operations, or create `static` variables for re-use.
If using a list, make it a static variable and clear it every time it is used. Re-use as much as possible.

### Avoid bloated code and unnecessary getters/setters.
This is situational, but in essence, what it means is to avoid using any sort of getters and setters unless absolutely necessary. Public or protected fields should suffice for most things.
If something needs to be encapsulated in the future, IntelliJ can handle it with a few clicks.

### Do not create methods unless necessary.
Unless a block of code is very large or used in more than 1-2 places, don't split it up into a separate method. Making unnecessary methods only creates confusion, and may slightly decrease performance.

---

## Key decisions

Binding constraints. Violating one of these makes a task wrong - when in doubt, follow them.

- Target the Mindustry **`v9` branch**, including APIs that only exist there. Stock (current release) compatibility is not a goal right now - it can be added later, when v9 releases. Reasoning: [`ai-artifacts/decisions/TargetV9.md`](ai-artifacts/decisions/TargetV9.md)
- New code goes into `java/mu/`. `old-src/` is reference only, never on the source path, never buildable.

---

## This mod is being rewritten for Mindustry v9

The mod is currently being **rewritten for Mindustry v9** (`../Mindustry`, branch `v9`,
Java 17 / Gradle 9).

Reasoning: adapting to Mindustry's API changes, reimagining lots of old code and implementing new features.

New code belongs in a fresh source tree; the pre-rewrite implementation is **kept for reference in `old-src/`** (committed, but do not treat it as buildable - `build.gradle.kts` compiles the new `java/` + `assets/` layout, so `old-src/` is never on the source path).

### Current repository layout

```
mod.hjson           mod metadata (main: mu.MUMain)
libs/               game jar used as compileOnly dependency (gitignored, CI downloads it)
.github/workflows/  CI: downloads BE jar, runs ./gradlew deploy
old-src/            pre-v9-rewrite sources + resources (structure: ai-artifacts/OldSrc.md)
assets/             location for resources like bundles or sprites
java/mu/            intended location for the new v9 sources
ai-artifacts/       AI-facing docs, class trees, task tracking (see below)
```

Full `old-src/` directory tree is described in `ai-artifacts/OldSrc.md` - read it when porting or
referencing pre-v9 code.

---

## AI artifacts

AI-facing documentation in `ai-artifacts/`. Nothing in here is loaded automatically - this file map is the
index: every doc with a one-line "read when". Open the file named in the task you are on.

Each `X-docs/` folder holds content files plus `XIndex.md`, a curated class tree of the
source it covers. Trees are curation, not dumps: only what this mod touches, with
relationships (extends/patches/reflects) and purpose per entry - the full tree is
derivable with `find`/`glob`, and stale hand-maintained entries make agents skip the
source that would correct them.

### File map

**Top level**
- `OldSrc.md` - porting or referencing pre-v9 code: old-src/ tree + per-file intent.
- `tasks/` - one folder per task (`<name>/task.md`, `notes.md`) plus `TaskStates.md`, the
  state table + pipeline header. Read that header before touching any task.
- `upstream.md` - last-synced Anuken/Mindustry v9 commit; state file of the
  `sync-upstream` skill.

**decisions/** - the "why" behind the key decisions above.
- `TargetV9.md` - why we target v9 APIs only, with no stock compatibility.

**arc-docs/** - Arc source (`../Mindustry/arc/arc-core/src/arc/`).
- `ArcIndex.md` - class tree: which Arc files/packages matter here. Read first when touching Arc code.
- `Element.md` - working with UI elements: Actor->Element renames, draw pipeline, invalidation, hit/touch rules.
- `Scene.md` - scene structure, input entry, focus, dialog stacking, resize behavior.
- `Events.md` - wiring listeners: dispatch phases, touch focus, concrete listener semantics.
- `Actions.md` - using actions: lifecycle, pooling, composition, deltas vs libGDX.
- `Drawable.md` - drawables/styles: the family, how to obtain one, how styles consume them.
- `Table.md` - laying out tables: cell model, Arc-only builder DSL, invalidation quirks.
- `Coordinates.md` - read before touching positions/sizes/scaling: spaces, conversions, Scl.
- `UI.md` - read before building or modifying UI: dialog anatomy, Table DSL, input wiring, gotchas.

**mindustry-docs/** - Mindustry source (`../Mindustry/core/src/mindustry/`).
- `MindustryIndex.md` - class tree: editor/dialogs/map-io subset relevant here. Read first when patching Mindustry.
- `MapEditor.md` - the editor model: state, key methods, undo entry points, gotchas.
- `MapEditorDialog.md` - the editor screen: build()-on-show rebuild, sub-dialog fields, lifecycle, save().
- `MapView.md` - editor canvas: camera/zoom, input handling, reflection-worthy private fields.
- `EditorTool.md` - the tool enum: shared mutable state, modes, keybinds, gotchas.
- `EditorRenderer.md` - chunked editor renderer: shared game shader, cache invalidation entry points.
- `EditorUndo.md` - how undo works: EditorTile recording, swap-based DrawOperation, OperationStack.
- `MapFiles.md` - how maps are stored: MSAV format, tags as source of truth, MapIO/SaveIO chains.
- `MapAssets.md` - v9 per-map data packs: DataManager, ordinal-coupled views, global-state mutation traps.
- `MapGenerateDialog.md` - generate filters: applied vs stored mode, preview threading, filter semantics.
- `MapObjectivesDialog.md` - objectives graph: live commit model, canvas sync, reflection-based field UI registry.
- `WaveInfoDialog.md` - waves editor: staged commit, WaveGraph rebuilding.
- `Editor.md` - read before patching the editor: entry points, UI/state hooks, what silently breaks.
- `Maps.md` - read before working with map files: listing/loading, reading rules/waves, saving from editor state.

**mu-docs/** - this mod (`java/mu/`, `old-src/`).
- `MUIndex.md` - entry point for mod-specific docs as the rewrite progresses.
- `Running.md` - read before building, installing, or launching the mod for testing.

### How to work with ai-artifacts

**Before writing or correcting any file in `ai-artifacts/`, load the `index-docs` skill**
(`.opencode/skills/index-docs/SKILL.md`) - it holds the writing rules: verify against
source, audience, length budgets, naming/linking, staleness. This section says *whether*
you may write; the skill says *how*.

Write direction depends on what you were asked to do:

- **Researching code, writing docs, or creating a task** -> **write into `ai-artifacts/`.**
  Put the class tree entries, term explainers, decision write-ups and task files where the
  file map above says they belong, and add a map line for every new file. Don't just report
  findings in chat and leave the files stale; the file is the deliverable.
- **Writing the mod's code from `ai-artifacts/`** -> **read from those files** and treat them
  as the source of context for the task. If you discover additional info worth keeping while
  working, **ask the user before writing it** - do not silently edit docs that other work may
  depend on. Same applies to corrections: surface what looks outdated instead of rewriting it.

Reading is always allowed; writing is either prompted by a research/doc/task request, or
gated on asking first.

---

## Mindustry / Arc source

Ground truth is source code, in this order:

1. `java/mu/` - this mod's new code.
2. `../Mindustry` - sibling checkout, branch `v9`. **Both projects live in this one
   checkout**, no separate Arc clone exists:
    - Mindustry: `../Mindustry/core/src/mindustry/` (e.g. `ui/dialogs/CustomRulesDialog.java`)
    - Arc: `../Mindustry/arc/arc-core/src/arc/` (e.g. `scene/Element.java`, `scene/ui/layout/Table.java`)
3. `old-src/` - pre-v9 code, for intent only, never the current API.

`ai-artifacts/` is a cache over (2) and (3), not a replacement. If a doc and the source
disagree, **the source is right and the doc is stale** - say so, don't quietly follow the doc.

The checkout sits outside this project, so a permission prompt when reading it is not the
same as it being missing. If it is missing or not on `v9`, say so and offer:

```
git clone -b v9 https://github.com/Anuken/Mindustry.git ../Mindustry
```

Only decline the task if it genuinely cannot proceed without source research - `libs/`
still lets you compile and run the mod without the checkout.

---

## When to read what

Check the file map above first - open a doc only if its line matches your task. The
relevant `...Index.md` maps which source files matter; otherwise read source.

- **Rewriting or porting old code:** `OldSrc.md` for what it did, *then* the current
  target's source for what you are patching. `OldSrc.md` never describes the current API.
- **"How does this system work?"** (Arc UI layout, events, rendering, coordinate handling):
  docs first - this is the knowledge source alone does not give you. If empty, read source
  and write it.
- **"What does this specific thing do?"** (signature, semantics, does it exist in v9):
  source first.
- **Reflection into private fields** (this mod does a lot of this): always read current
  source first. A field renamed in v9 fails silently or returns null, not loudly.
- **Running the task pipeline** (syncing upstream, creating/implementing/reviewing/
  testing tasks): the `sync-upstream`, `create-tasks`, `implement`, `review`, `test`
  skills; states + transitions in the `TaskStates.md` header.
- **About to build UI or touch coordinates:** `arc-docs/UI.md` and
  `arc-docs/Coordinates.md` (file map).
- **Stuck after two reads of the same source:** stop and ask, rather than re-reading and
  guessing.

Writing, as in the section above: research/docs/tasks -> write into `ai-artifacts/`;
coding -> read, and ask before writing anything new you discover. Worth writing is
v9-only API differences, non-obvious structure, gotchas, and why something is the way it
is - cite path + symbol name (never line numbers) and the branch it was observed on.
Not worth writing: code excerpts, anything one `read` already reveals, anything local to
one task (that goes in `tasks/`).
