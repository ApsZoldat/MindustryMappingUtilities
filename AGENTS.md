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

## Key decisions

Binding constraints. Violating one of these makes a task wrong - when in doubt, follow them.

- Target the Mindustry **`v9` branch**, including APIs that only exist there. Stock (current release) compatibility is not a goal right now - it can be added later, when v9 releases. Reasoning: [`ai-artifacts/decisions/TargetV9.md`](ai-artifacts/decisions/TargetV9.md)
- New code goes into `java/mu/`. `old-src/` is reference only, never on the source path, never buildable.

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

## ai-artifacts

AI-facing documentation. Nothing in here is loaded automatically - this section is the
index, so read the file named in the task you are on.

```
ai-artifacts/
├── OldSrc.md               old-src/ directory tree + per-file notes (reference only)
├── decisions/              long-form reasoning behind key decisions
│   └── TargetV9.md             why we target the v9 branch over stock compatibility
├── tasks/                  current and future tasks, and everything related to them
├── mindustry-docs/
│   └── MindustryClassTree.md   class tree of Mindustry + list of terms/systems used by it
├── arc-docs/
│   └── ArcClassTree.md         class tree of Arc + list of terms/systems used by Arc
└── mu-docs/
    └── MUClassTree.md          class tree of this mod + list of terms/systems used by it
```

Each `...ClassTree.md` holds a class tree plus a list of terms/systems specific to that
codebase. Entries in those lists are empty for now; when one gets filled in, it becomes a
separate `.md` file in the same folder explaining that topic from scratch
(the `Element` class in Arc, for example, should lead to `arc-docs/Element.md` explaining how
Arc's UI works).

`OldSrc.md` is reference material only - it does not get an entry list.
`decisions/` holds the reasoning behind the key decisions above - the decision itself is
listed in `AGENTS.md`, the "why" lives here.
`tasks/` holds task descriptions, notes and artifacts; unrelated to the docs folders.

### How to work with ai-artifacts

Write direction depends on what you were asked to do:

- **Researching code, writing docs, or creating a task** -> **write into `ai-artifacts/`.**
  Put the class tree entries, term explainers, decision write-ups and task files where the
  index above says they belong. Don't just report findings in chat and leave the files stale;
  the file is the deliverable.
- **Writing the mod's code from `ai-artifacts/`** -> **read from those files** and treat them
  as the source of context for the task. If you discover additional info worth keeping while
  working, **ask the user before writing it** - do not silently edit docs that other work may
  depend on. Same applies to corrections: surface what looks outdated instead of rewriting it.

Reading is always allowed; writing is either prompted by a research/doc/task request, or
gated on asking first.
