<!-- verified: v9 @ 2026-10-09, sources: java/mu/MU.java, java/mu/modules/MUModule.java, java/mu/modules/RulesDialogModule.java, java/mu/SettingsDialogMod.java, core/src/mindustry/ui/dialogs/CustomRulesDialog.java -->

# Module system

How this mod boots features as `MUModule` units. Read before adding, renaming or
debugging a module.

## Layout and registration

- `mu.MU` - mod entry point. On `ClientLoadEvent` it registers modules (one line each,
  above `SettingsDialogMod.enable()`), then runs the init loop. The settings category
  reads `MU.modules` synchronously, so registration must come first.
- `mu.modules.MUModule` - unit base: `name` is both the id and the settings key, `def`
  its default; `enabled()` reads `settings.getBool(name, def)`, `init()` runs once and
  may throw.
- `mu.modules.RulesDialogModule` - the rules-row injector (key `mu_rules_dialog`).
- `mu.SettingsDialogMod` - the settings category; not a module, always on.

The init loop skips disabled modules and catches `Throwable` from `init()`: failure is
logged plus one startup notice listing the module and error. The module stays in
`MU.modules` and nothing is persisted - retry requires a restart. Feature toggles are
plain settings; the settings UI shows the "restart to apply" notice.

Keys are `mu_`-prefixed strings, deliberately distinct: `mu_rules_dialog` is the
MODULE key (rendered by the `MU.modules` loop, restart-to-apply, gates `init()`);
`mu_hidden_rules` is a separate SETTING (the rules_dialog section's `checkPref` row)
that gates the injected rows, read live on every rebuild; `mu_env_settings` gates the
environment-settings button, also read per rebuild.

## RulesDialogModule

`init()` discovers the four v9 `CustomRulesDialog` instances by reflection - editor map
info, editor playtest, custom game, pause menu - via `findDialog` (a `String...` field
path; a renamed field throws at init). `hookDialog` registers per dialog with
its own try/catch: a failure logs `Failed to hook ... rules dialog` and skips that
dialog; zero hooked dialogs throws, which disables the module for the session.

Each dialog gets one closure in `CustomRulesDialog.additionalSetup`, which is **static**
in v9 (`public static Seq<Runnable>`): the compiled access is `getstatic`, so
`IncompatibleClassChangeError: Expected static field ... additionalSetup` at startup
means the running game binary predates the instance-to-static change - rebuild the
game; the mod targets v9 HEAD.

Because the list is shared, one dialog's rebuild runs every closure; each guards itself:

1. `failed` flag - after the first throw that closure never runs again (logged once).
2. `mu_hidden_rules` setting off - the rows feature's atomic toggle, read live so
   switching applies on the next rebuild without a restart.
3. `rules == null` - this dialog was never shown, yet its closure still fires during
   other dialogs' rebuilds.
4. `categoryNames.contains("miscellaneous")` - marker: `setupMain` clears
   `categoryNames` and rebuilds every row, so the injected category's presence means
   this dialog already carries this build's rows.

Closures run after vanilla built its rows (`additionalSetup` runs before the categories
are assembled for display), so `category()` can reuse vanilla category tables by name
and create only `miscellaneous`. Team rows walk the teams category's tables and
identify each team by its toggle button's label: search can hide a whole team table,
which shifts positions. The environment button opens `envDialog` - 8 flags over the
immutable `Rules#env` set, with an `any`-safe rebuild when unchecking.

Failure scope: per dialog at init, per closure at rebuild, per module at `init()` -
nothing propagates into vanilla's row building.
