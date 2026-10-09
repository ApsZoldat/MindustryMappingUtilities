# MUModule framework

## Goal

`MUModule` abstraction + MUMain lifecycle: every module is an atomic unit whose
`init()` runs once at startup; a failing module is disabled for the session (logged,
listed in a startup notice), never affecting other modules. Toggles in settings are
restart-to-apply - there is no runtime enable/disable.

## Scope

- `java/mu/MUModule.java` (new)
- `java/mu/MUMain.java` (module registry + init loop + failure notice)
- No settings UI here, no concrete modules (those are dependent tasks).

## Plan steps

1. `MUModule`: `public abstract class` with `public final String name` and
   `public final boolean def` set via constructor, `public abstract void init()`
   (may throw), and `public boolean enabled()` = `settings.getBool(name, def)`.
   The setting key string lives in each subclass's constructor via `super(...)`.
2. `MUMain`: a `public static Seq<MUModule> modules` registry, filled on
   `ClientLoadEvent` (dialog instances exist by then - old-src did the same).
   MUMain itself instantiates and adds the modules; dependent tasks append their
   `modules.add(new ...)` lines here.
3. Init loop: for each module - `if(!module.enabled()) continue;` (a disabled
   module is skipped entirely, no init call; this is the only place
   `enabled()` is consulted), then `try{ module.init(); }catch(Throwable t){ ... }` -
   on failure `Log.err` with the full stack trace, add `name + " - " + message` to
   a failure list, do NOT remove the module from the registry (its `enabled()`
   still answers, but its init never ran again this session).
4. If the failure list is non-empty after the loop: startup notice via
   `ui.showInfo(...)` (v9 `mindustry.core.UI`, NOT the removed `ui.Dialogs`)
   listing failed module names + one-line messages, plus "disabled for this
   session, see logs" wording; full traces only in the log.

## Acceptance criteria

- A module whose `init()` throws: other modules still initialize; game does not crash.
  (Verified during `rules-dialog-module` implementation or with a temporary
  scratch module in a local run - this task's diff registers no real modules, so
  the loop is exercised by the first dependent task.)
- The startup notice appears once, lists exactly the failed modules with one-line
  messages, and is dismissible with OK. (Same verification path as above.)
- A module whose setting is false: its `init()` is never called.
- A failed module's `init()` is not retried until the game is restarted.
- Nothing anywhere calls enable/disable at runtime; disabling takes effect only
  after restart (verified by reading the diff - no listeners to remove).

## Constraints

- Restart-to-apply semantics: no `disable()`, no runtime toggling, no revert.
- Session-only auto-disable: never persist a failure into settings (Q2 decision).
- Catch `Throwable` in the init loop; per-module try/catch, continue on failure.
- `arc.struct.Seq`, `arc.func.*` only - no `java.util.function`/collections.
- Class-specific setting keys via `super("name", def)` in the subclass
  constructor (Q1 decision) - no annotations, no reflection scanning.
- Code style per AGENTS.md (wildcard imports, same-line braces, camelCase).
- Client-only by design: `ClientLoadEvent` does not fire on headless servers;
  this mod is editor/UI-only, so no headless guard is needed.

## Refs

- `ai-artifacts/decisions/MUModules.md` - the philosophy this implements
  (written in the same batch; read first).
- `old-src/java/mu/mods/MUMod.java` - intent only: old live enable/disable model
  that this replaces.
- v9 `/root/projects/Mindustry/core/src/mindustry/core/UI.java` - `showInfo(String)`
  (the old `mindustry.ui.Dialogs` class no longer exists; `Vars.ui` is
  `mindustry.core.UI`).

## Open questions

- None.
