# Notes - MUModule framework

## What changed

Implemented the module framework per `task.md` (plus a follow-up rename requested
by the user: `MUMain` -> `MU`, `MUModule` -> `mu.modules.MUModule`, `[MU]` log
prefix):

- **`java/mu/modules/MUModule.java` (new, package `mu.modules`)** - abstract base:
  `public final String name`
  (the setting key), `public final boolean def`, both set by the subclass
  constructor (`super(name, def)`); `enabled()` = `settings.getBool(name, def)`
  (explicit default, no dependence on the settings menu); `public abstract void
  init()` which may throw.
- **`java/mu/MU.java`** (renamed from `MUMain`) - added `public static
  Seq<MUModule> modules` (`import mu.modules.*;`) and a
  `ClientLoadEvent` handler that (1) registers modules (placeholder comment marks
  where dependent tasks append their `modules.add(new ...)` lines) and (2) runs
  the init loop: skip when `!module.enabled()` (the only `enabled()` call site in
  the framework), otherwise `try{ init() }catch(Throwable t)` -> `Log.err(msg, t)`
  (full stack trace in the log) + append `name - message` to a local failure list.
  The module is **not** removed from the registry and its failure is never written
  to settings.
- **`mod.hjson`** - `main: "mu.MUMain"` -> `main: "mu.MU"`.
- All `Log` calls carry a `[MU] ` prefix (`Log.info("[MU] ...")`,
  `Log.err("[MU] ...", t)`).
- After the loop, a non-empty failure list produces one `ui.showInfo(...)`
  (v9 `mindustry.core.UI`, OK button, dismissible) listing exactly the failed
  modules with one-line messages + "disabled for this session" / "see the log"
  wording. Shown via `Time.runTask(4f, ...)`, the same delayed-startup-notice
  pattern `Control` uses for its own `ClientLoadEvent` dialogs.
- Null-message throwables (e.g. NPEs) fall back to the exception class name so the
  dialog line is never empty.
- Kept the pre-existing "hii :3" log line (now with the `[MU]` prefix).

No settings UI, no concrete modules, no enable/disable/revert paths (scope of
dependent tasks `mu-settings-category`, `rules-dialog-module`).

## Changed files

- `java/mu/modules/MUModule.java` (new, package `mu.modules`; `java/mu/MUModule.java` removed)
- `java/mu/MU.java` (renamed from `java/mu/MUMain.java`, which was removed)
- `mod.hjson` (`main` -> `mu.MU`)

## Build

`./gradlew build` green after the change (baseline was green before it).

## Human test checklist

No real modules are registered by this diff, so the loop is exercised by the
first dependent task - or locally with a temporary scratch module:

1. Scratch module whose `init()` throws -> game keeps loading, other modules
   still init, no crash; a single info dialog appears listing that module name +
   one-line message, dismissible with OK; full stack trace only in the log.
2. Scratch module with setting `false` -> its `init()` is never called (log a
   marker line inside `init()` to confirm).
3. After a failed init, restart is required for a retry: with the same scratch
   module, the session shows the notice once (no repeat dialog mid-session), and
   after restarting it appears again (failure not persisted to settings).
4. All-fine run (no failures, no disabled modules) -> no dialog at all.
5. Settings category (task `mu-settings-category`) must read `MU.modules`
   and see entries only after `ClientLoadEvent` - registration happens there.
