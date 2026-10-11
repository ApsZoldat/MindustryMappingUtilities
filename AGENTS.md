# AGENTS.md

Instructions for AI agents working on this repository.

## Basic context

- **[Mindustry](https://github.com/Anuken/Mindustry/tree/master)** - An automation tower defense RTS game written on Java.
    - Work specifically with [V9 branch](https://github.com/Anuken/Mindustry/tree/v9) of Mindustry
- **[Arc](https://github.com/Anuken/Arc)** - Mindustry's gamedev framework based off of libGDX.
    - Mindustry V9 has the entirety of Arc included in its repo, instead of being an external dependency.
- **[Mindustry Mapping Utilities](https://github.com/ApsZoldat/MindustryMappingUtilities)** - This mod for Mindustry, aims to improve map editor's convenience and possibilities.

### Arc vs libgdx

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
- Use wildcard imports - `import some.package.*` - for everything.

### Comments and logging
- Inline `//` comments are sentences: start with a capital letter and put one space after the slashes - `// Like this`, not `//like this`.
- Every log message carries this mod's `[MU] ` prefix: `Log.info("[MU] ...")`, `Log.err("[MU] ...")`.

### Do not use incompatible Java features (java.util.function, java.awt, java.lang.Objects).
Android and RoboVM (iOS) do not support many of Java 8's features, such as the packages `java.util.function`, `java.util.stream` or `forEach` in collections. Do not use these in your code.
If you need to use functional interfaces, use the ones in `arc.func`, which are more or less the same with different naming schemes.

The same applies to any class *outside* of the standard `java.[n]io` / `java.net` / `java.util` packages: Most of them are not supported.
`java.awt` is one of these packages: do not use it, ever. It is not supported on any platform, even desktop - the entire package is removed during JRE minimization.

**Note for this mod:** these rules are written for the game's multiplatform constraints.
The v9 target is Java 17, but keep following them (especially `arc.func` instead of `java.util.function`, and no `java.awt`)

### Use `arc` collections and classes when possible.
Instead of using `java.util.List`, `java.util.HashMap`, and other standard Java collections, use `Seq`, `ObjectMap` and other equivalents from `arc.struct`.
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

- The mod is currently being **rewritten for Mindustry v9**. Target the Mindustry **`v9` branch** found in `../Mindustry` and `libs/Mindustry.jar` (use `../Mindustry` whenever possible), including APIs that only exist there. Stock (current release) compatibility is not a goal.
- New code goes into `java/mu/`. `old-src/` is reference only, never on the source path, never buildable.

---

### Current repository layout

```
mod.hjson           mod metadata (main: mu.MU)
libs/               game jar used as compileOnly dependency (gitignored, CI downloads it)
.github/workflows/  CI: downloads BE jar, runs ./gradlew deploy
old-src/            pre-v9-rewrite sources + resources (structure: ai-artifacts/OldSrc.md)
assets/             location for resources like bundles or sprites
java/mu/            intended location for the new v9 sources
ai-docs/            AI-facing docs, class trees, task tracking (see below)
```

Full `old-src/` directory tree is described in `ai-artifacts/old-src.md` - read it when porting or
referencing pre-v9 code.

---

## Mindustry / Arc source

Ground truth is source code, in this order:

1. `java/mu/` - this mod's new code.
2. `../Mindustry` - sibling checkout, branch `v9`. **Both projects live in this one
   checkout**, no separate Arc clone exists:
    - Mindustry: `../Mindustry/core/src/mindustry/` (e.g. `ui/dialogs/CustomRulesDialog.java`)
    - Arc: `../Mindustry/arc/arc-core/src/arc/` (e.g. `scene/Element.java`, `scene/ui/layout/Table.java`)
3. `old-src/` - pre-v9 code, for intent only, never the current API.

`ai-docs/` is a cache over (2) and (3), not a replacement. If a doc and the source
disagree, **the source is right and the doc is stale** - say so, don't quietly follow the doc.

The checkout sits outside this project, so a permission prompt when reading it is not the
same as it being missing. If it is missing or not on `v9`, say so and offer:

```
git clone -b v9 https://github.com/Anuken/Mindustry.git ../Mindustry
```

Only decline the task if it genuinely cannot proceed without source research - `libs/`
still lets you compile and run the mod without the checkout.
