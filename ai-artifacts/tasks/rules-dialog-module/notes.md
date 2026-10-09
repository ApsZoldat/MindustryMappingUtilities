# Notes - RulesDialogModule (hidden checks port)

## What changed

Implemented `java/mu/modules/RulesDialogModule.java` (moved from `java/mu/`, package
`mu.modules`) per `task.md`: a `MUModule`
(`super("mu_rules_dialog", true)` - renamed from `mu_hidden_rules` per user request)
that injects the hidden rule rows into every v9 `CustomRulesDialog`.

Two distinct keys, per user - do not merge them again:

- `mu_rules_dialog` - the MODULE key: rendered by the `MU.modules` settings loop,
  restart-to-apply, gates `init()`.
- `mu_hidden_rules` - a separate SETTING in the settings rules_dialog section
  (its `checkPref` row in `SettingsDialogMod`, its own bundle label), read live
  at the top of `setup()`; this is the task's "one atomic toggle" for the whole
  row set.

### Discovery (`init()`)

Four reflection paths, one `try/catch` each (a failed path logs
`[MU] Failed to hook ... rules dialog` and skips that dialog only; zero hooked
throws `no rules dialogs found` so the framework disables the module):

- `Vars.ui.editor.infoDialog.ruleInfo` (editor map info)
- `Vars.ui.editor.playtestDialog.dialog` (editor playtest)
- `Vars.ui.custom.dialog.dialog` (custom game / play menu)
- `Vars.ui.paused.rulesDialog` (pause menu)

All four are the complete set of `new CustomRulesDialog` sites in v9 (verified
by grep), each assigned eagerly in its host's constructor, so they all exist at
`ClientLoadEvent`. Dedupe via `Seq.contains(dialog, true)` (identity).

### Per-rebuild closure (static `additionalSetup`)

One closure per hooked dialog, capturing its dialog, registered by direct field
access (`CustomRulesDialog.additionalSetup.add(...)` - the field is `public static`
at v9 HEAD; an earlier reflective access was reverted per user decision: it settled
as static+public in v9, and an `IncompatibleClassChangeError: Expected static field
... additionalSetup` means the *game binary* predates the instance-to-static change
and must be rebuilt). Four guards, in order:

1. `boolean[] failed` - after the first throw the closure never runs again
   (logged once, that dialog's rows stay at whatever was added - no per-keystroke
   log spam, no aborting vanilla's `setupMain`).
2. `settings.getBool("mu_hidden_rules", true)` false -> return (the separate
   feature setting, read live - applies on the next rebuild, no restart).
3. `Reflect.get(dialog, "rules") == null` -> return (never-shown dialog; the
   static list runs this closure during *other* dialogs' rebuilds too).
4. `dialog.categoryNames.contains("miscellaneous")` -> return (marker: fresh
   builds clear `categoryNames`, so its presence means this dialog already has
   this build's rows - also what keeps other dialogs' rebuilds cheap).

Rows go through a `category(dialog, name)` helper that reuses an existing
vanilla category (`categories.get(categoryNames.indexOf(name))`) or creates one
(miscellaneous). Display order is unaffected by call order: `category()` appends
only when new, so miscellaneous always renders last.

### Rows (matches `RulesSurvey.md` "ported" list exactly)

- waves `hideSpawns` / resourcesbuilding `ghostBlocks` / unit `possessionAllowed`,
  `unitPayloadUpdate` / enemy `pvpAutoPause`, `coreDestroyClear` /
  environment `borderDarkness`, `disableOutsideArea`, `staticFog`,
  `dragMultiplier`, 3 color swatch rows (manual `bundle.get(key)...contains`
  search gate, same as vanilla's `ambientLight` row), env-settings button
  gated by `settings.getBool("mu_env_settings", true)` /
  miscellaneous `canGameOver` + `modeName`/`mission` via v9
  `text(..., s -> true, () -> true)`.
- team: `cheat` + `aiCoreSpawn` inside each base team's `Collapser`.

Skip-list honored: no `logicUnitBuild`, no `playerteam`/`enemyteam` pickers, no
multipliers v9 renders, no `Version.build` showSpawns branch, no category-fix
listener (v9 clears `categories`/`categoryNames` itself).

### Team walk

After vanilla's team loop, `additionalSetup` runs with `current` = teams
category; the walk reads `categories`/`categoryNames` by name (not `current`),
finds cells holding a `Table` with a `Collapser` child, and sets
`current = Reflect.get(collapser, "table")` to add the two checks (restored at
the end). **Team identity comes from the toggle button's label**
(`TextButton.getLabel().getText()` vs `Team.baseTeams[i].coloredName()`),
advancing monotonically - vanilla *drops whole team tables* when the search
filters every vanilla row of that team, so pure positional mapping would write
`cheat` to the WRONG team after a skip.

Known edge (documented, matches vanilla): searching a term that matches no
vanilla team row (e.g. `cheat`) removes the team tables entirely, so the team
checks have nothing to attach to and don't show - the same way vanilla's own
team rows vanish. All other added rows filter normally.

### Environment dialog

Old-src `BaseDialog` layout (warning + pane + 8 checkbox/description rows),
rewritten for the immutable API: `rules.env.has(Env.x)` for state,
`with(env)` on check; on uncheck `Environments.of(Env.all.toArray(Env.class))`
(typed local - the raw `Class` in `Seq.toArray` makes the `Environments.of`
overloads ambiguous inline) `.without(env)` when `rules.env.isAny()`, else
`without(env)` directly. `setChecked` runs *before* the `changed` listener so
opening the dialog can't mutate `rules.env` (programmatic change events are off
by default).

### Bundle - no changes needed

`assets/bundles/bundle.properties` already contains every key the rows need
(labels, all `.info` tooltips, `rules.env.*` + descriptions + warning,
`rules.title.miscellaneous`, `rules.environmentsettings`); `rules.hidespawns`
and `rules.title.environment` come from vanilla v9. Both setting labels exist:
`setting.mu_rules_dialog.name` (module row) and `setting.mu_hidden_rules.name`
(setting row). Out-of-scope keys already present in that file (`revealedblocks`,
`planetbackground`, `numberedteam`, `unitammo`, `infiniteammo`) were left
untouched. `RulesSurvey.md` footnotes confirm `numberedteam`/`unitammo`/
`infiniteammo` have no v9 backing field.

### Build environment note (CI caveat)

`libs/Mindustry.jar` was a local build from 2026-10-08 09:27, predating the
`Env refactor` commit (`3ed5ff9c64`, same day 17:05) that this task targets;
it had `int env` and no `Environments`. The user refreshed the jar
(2026-10-09 06:10, has `Environments`) so the local build compiles.
**CI downloads the latest public BE (27965, 2026-10-02) which predates the
refactor too** - CI `compileJava` will fail on the env dialog until Anuken
publishes a post-refactor BE (or the workflow is changed to build against the
v9 checkout). Not fixable from this repo's code.

## Changed files

- `java/mu/modules/RulesDialogModule.java` (new; moved from `java/mu/`, package
  `mu.modules`; user-directed details: methods renamed `find`/`hook` to
  `findDialog`/`hookDialog`, `findDialog` keeps `String...` varargs, `mu_hidden_rules`
  read live as the rows gate, all inline comments capitalized)
- `java/mu/MU.java` (one line: `modules.add(new RulesDialogModule());` at the
  registration placeholder; inline comments capitalized)

`java/mu/SettingsDialogMod.java` and `assets/bundles/bundle.properties` have **no net
change from this task**: the `checkPref("mu_hidden_rules", true)` row and the
`setting.mu_hidden_rules.name` label are the separate setting this module reads, so
they stay as `mu-settings-category` specced them (its grep acceptance - the literal
in `SettingsDialogMod` and the one in `RulesDialogModule` matching - still holds via
the `setup()` gate).

Conventions for future work live in `AGENTS.md` ("Comments and logging"): capitalized
`// Comment` style and the `[MU] ` log prefix.

## Build

`./gradlew build` green after the change (baseline was green before it).

## Human test checklist

Normal paths:

1. All four dialogs - editor map info, editor playtest, custom game
   customize, pause menu - show the ported rows in the right categories, with
   `.info` tooltips on hover (`rules.ghostblocks.info` etc.).
2. Toggle any check / edit a number or text row, close, reopen -> state
   persists; save the map, reopen it -> persisted in the map file.
3. Fog color rows: picker opens, swatch updates live; value survives reopen.
4. Team rows: expand a base team's collapser -> `cheat`/`Cores Spawn Ships`
   inside; toggling team 3 does NOT change team 0-2 values (label matching).
5. Miscellaneous category renders last, `can Game Over`/`Mode Name`/`Mission`
   round-trip (empty string -> null -> empty field).

Environment dialog:

6. Open env dialog -> 8 checkboxes reflect `rules.env`; toggle flags, close,
   reopen -> persist; save/reload map -> persist.
7. Map whose env is "any" (all 8 visually checked): merely OPEN the dialog and
   close -> `rules.env` unchanged (no `setChecked` side effect); uncheck one
   flag -> no crash, remaining 7 stay set (rebuild-all-minus-one path); recheck
   it.
8. Env dialog opened from a map with only some flags: uncheck an unchecked-
   impossible / check an uncheck path... verify normal `with`/`without` round-trip.

Search / rebuild:

9. Type `ghost` -> only ghost (and matching) rows remain; clear -> all back;
   repeat typing several times -> no duplicated rows anywhere.
10. Search `cheat` -> team section absent (vanilla behavior, known edge); no
    error; other dialogs' rows unaffected.
11. Open editor dialog, then playtest dialog, then back -> no duplicates in
    either (marker guard across the static `additionalSetup`).
12. Search keystroke in one dialog must not add rows to another dialog's
    already-built categories (check a second dialog after searching in the
    first).

Toggles / failure:

13. `mu_rules_dialog` false + restart -> module never inits (no added rows, no
    `[MU] Hooked` logs); true + restart -> back. `mu_hidden_rules` false ->
    added rows disappear on the next rebuild (live, no restart); true -> back.
    `mu_env_settings` false -> env button gone, other rows remain (and vice
    versa).
14. Scratch-build reflection failure (rename `infoDialog` in a scratch jar):
    that dialog gets no rows, exactly one `[MU] Failed to hook ...` log at
    init, other three work; rename all four -> module disabled, single startup
    failure notice listing `mu_rules_dialog`.
15. Normal run: no `[MU] Failed ...` lines in the log at all.
16. Pause menu rules: edits apply when the rules dialog hides (copy applied on
    hide - `PausedDialog` behavior) and show up after saving.
