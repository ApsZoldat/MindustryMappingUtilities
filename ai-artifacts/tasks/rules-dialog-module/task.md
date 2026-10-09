# RulesDialogModule (hidden checks port)

## Goal

Port `old-src/java/mu/mods/RulesDialogMod.java` into `java/mu/modules/RulesDialogModule.java`
(a `MUModule`), adding the normally-hidden rule checks to every
`CustomRulesDialog` instance. Only hidden checks - banned-content-dialog
improvements and planet background are explicitly out of scope.

## Scope

- `java/mu/modules/RulesDialogModule.java` (new)
- `java/mu/MU.java` (instantiate + register the module)
- `assets/bundles/bundle.properties` (English rule labels, `.info` tooltips, env
  descriptions, `rules.title.miscellaneous`)

## Plan steps

1. Discovery in `init()`: find all v9 `CustomRulesDialog` instances by reflection:
   - `MapInfoDialog.ruleInfo` (via `Vars.ui.editor`'s info dialog)
   - `MapPlayDialog.dialog` (play menu + editor playtest dialog)
   - editor `MapEditorDialog`'s info dialog `ruleInfo`
   - `PausedDialog.rulesDialog`
   Verify each field name against current v9 source before use (reflection
   failure = silent null). Dedupe by identity (the editor info dialog may be
   reachable via two paths). Per-dialog `try/catch`: log the error, skip that
   dialog, continue; only if **zero** dialogs were hooked, throw so the framework
   disables the module for the session.
2. Per hooked dialog register one `Runnable` into
   `CustomRulesDialog.additionalSetup` (v9: **static** `Seq<Runnable>`). Each
   closure captures its dialog instance; guard against double-injection for the
   same build (see Constraints).
3. Closure body per rebuild: read `rules` via `Reflect.get(dialog, "rules")`
   (package-private field); **if null, return immediately** - because
   `additionalSetup` is static, this closure also runs while OTHER dialogs
   rebuild, and a dialog that was never shown yet has `rules == null`; ensure the
   target category exists via the old `category(name)` helper (reuse existing
   category by name, else `dialog.category(name)`), then add rows via v9 public
   builders `check`/`number`/`numberi`/`text` (these already filter by
   `ruleSearch` internally, so no manual search checks for standard rows;
   `ruleInfo` is only the tooltip-attachment helper, it does not filter).
4. Port rows (v9 still lacks all of these; verified against v9 `setupMain`):
   - waves: `hideSpawns` (vanilla bundle already has `rules.hidespawns`)
   - resources & building: `ghostBlocks`
   - unit: `possessionAllowed`, `unitPayloadUpdate`
   - enemy: `pvpAutoPause`, `coreDestroyClear`
   - environment: `borderDarkness`, `disableOutsideArea`, `staticFog`,
     `dragMultiplier` (number), the static/dynamic/clouds **color rows**
     (button + `ui.picker.show(color, color::set)`), and the environment-settings
     button (only when `settings.getBool("mu_env_settings", true)` is true)
   - miscellaneous (new category, `rules.title.miscellaneous`): `canGameOver`,
     `modeName` and `mission` as `dialog.text(...)` rows (v9 `text` takes
     `valid` + `condition` - pass `() -> true`)
   - teams: per-base-team `cheat` and `aiCoreSpawn` (`coresspawnships`) checks by
     walking the teams category cells to the per-team `Collapser`s (structure
     verified in v9 `setupMain` team loop; `Collapser.table` field still exists)
   - gated by `settings.getBool("mu_hidden_rules", true)`; the whole feature set
     is one atomic toggle.
5. Environment dialog: same BaseDialog layout as old-src, but rewritten for the
   v9 immutable `Environments` API: `rules.env.has(Env.x)` for state,
   `rules.env = rules.env.with(Env.x)` / `.without(Env.x)` on toggle.
   `without` throws on `Environments.any` - handle by rebuilding from
   `Environments.of(Env.all.toArray(...))` minus the removed flag (or catch and
   rebuild). Port the 8 env checkboxes + descriptions from the old bundle.
6. Bundle: copy needed English keys from
   `old-src/resources/bundles/bundle.properties` (labels, `.info` tooltips,
   `rules.env.*` descriptions + warning, `rules.title.miscellaneous`). Skip keys
   vanilla v9 already defines (grep first, e.g. `rules.hidespawns`), and skip
   out-of-scope keys (`revealedblocks`, `planetbackground`, `numberedteam`,
   `unitammo`, `infiniteammo`).

## Acceptance criteria

- Opening any of the hooked rules dialogs shows all ported rows in the right
  categories, with `.info` tooltips on hover where present.
- Each check/number/text row reads and writes the real `Rules` field: toggle it,
  close, reopen the dialog - state persists; save and reopen the map - state in
  the map file.
- The fog color rows open the color picker and the swatch updates live.
- The env dialog toggles all 8 env flags and survives map save/reload; the
  "any environment" case does not crash (no `without` on `any`).
- Team `cheat`/`aiCoreSpawn` rows appear inside each base team's collapser and
  write `rules.teams.get(team)` values.
- Typing in the dialog's search field filters the added rows like native rows;
  repeated searches do not duplicate rows.
- Setting `mu_hidden_rules` false hides all added rows (live, next rebuild);
  `mu_rules_dialog` false + restart skips the module entirely; other
  dialogs in the game still show theirs (per-dialog independence).
- Simulating a reflection failure on one dialog (rename a field in a scratch
  build): that dialog gets no rows, the failure is logged once at init, the
  other dialogs still work; breaking all dialogs disables the module with
  the startup failure notice.

## Constraints

- Per-dialog graceful degradation (Q3): a failed dialog logs + skips; only
  zero-dialogs-hooked throws.
- `additionalSetup` is static in v9 and `setupMain` re-runs on **every search
  keystroke** and on every rebuild of ANY dialog: every closure must be cheap
  and must not inject into non-fresh builds. Two guards per closure:
  (a) `Reflect.get(dialog, "rules") == null` -> return (never-shown dialog);
  (b) an already-injected marker for the current build - the v9-safe check is
  `!dialog.categoryNames.contains("miscellaneous")` right before the first
  `category("miscellaneous")` call, since `setupMain` clears `categoryNames` at
  the start of each fresh build (verify during implementation; pick the
  simplest correct guard).
- No `categoryNames`-fix `VisibilityListener` (v9 fixed the bug - `setupMain`
  clears `categories` + `categoryNames`).
- Skip rows v9 already renders natively: `logicUnitBuild`, `playerteam`/
  `enemyteam` (team pickers), all unit/build multipliers present in v9
  `setupMain` - do not double-add.
- `Version.build`-based `hideSpawns`/`showSpawns` branch is dead - always
  `hideSpawns`.
- mu_-prefixed feature-toggle keys as string literals with explicit defaults;
  keys must match `mu-settings-category` exactly.
- Explicit default on every `settings.getBool`.
- No main-loop allocation in per-keystroke paths beyond what vanilla's own rows
  allocate; reuse `Tmp`/statics where the old code did.
- Code style per AGENTS.md; arc collections only.

## Refs

- `old-src/java/mu/mods/RulesDialogMod.java` - the feature list and intent (NOT
  the API: v9 changed `rules.env` to `Environments`, `text`, and static
  `additionalSetup`).
- `old-src/resources/bundles/bundle.properties` - English strings to port.
- v9 `/root/projects/Mindustry/core/src/mindustry/ui/dialogs/CustomRulesDialog.java` -
  builders `category/check/number/numberi/text/ruleInfo/team`, static
  `additionalSetup`, `rules` field, team-loop structure, `ruleSearch` filtering.
- v9 `/root/projects/Mindustry/core/src/mindustry/game/Rules.java` - field names
  (`hideSpawns`, `ghostBlocks`, ... `TeamRule`, `teams.get(Team)`).
- v9 `/root/projects/Mindustry/core/src/mindustry/world/meta/Environments.java`
  and `Env` - immutable env set (`has/with/without`, `any` throws on `without`).
- v9 `arc/scene/ui/layout/Collapser.java` - package-private `table` field
  (reflection target).
- `ai-artifacts/mindustry-docs/MapEditor.md`, `MapEditorDialog.md`,
  `Maps.md` - editor/dialog/map context.
- `ai-artifacts/tasks/mu-module-framework/task.md` - module contract.

## Open questions

- Exact v9 reflection paths for the editor dialogs (`Vars.ui.editor`'s info
  dialog vs `MapEditorDialog.infoDialog` may be the same object) - resolve
  during implementation by reading current source, dedupe by identity.
- Whether `PausedDialog.rulesDialog` behaves with in-game rules (it edits live
  `state.rules`); if it misbehaves, exclude it from discovery.
