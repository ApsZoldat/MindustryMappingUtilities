# MU settings category

## Goal

Always-enabled modification of the vanilla settings menu: an "Editor Settings"
category with section titles, module on/off toggles (restart-to-apply, with the
restart notice), and rules-dialog feature toggles. String-literal keys with an
explicit default on every read - no holder class (Q6 decision).

## Scope

- `java/mu/SettingsDialogMod.java` (new - static `enable()` like old-src; NOT a
  `MUModule`, it is always enabled)
- `java/mu/MU.java` (call `SettingsDialogMod.enable()` on `ClientLoadEvent`)
- `assets/bundles/bundle.properties` (English `settings.*` keys only)

## Plan steps

1. Port the `Title` inner class (`SettingsMenuDialog.SettingsTable.Setting`
   subclass, title + optional bottom text) from
   `old-src/java/mu/mods/SettingsDialogMod.java`; verify v9 `Setting` still has
   the `title` field and overridable `add(SettingsTable)` (it does - see Refs).
2. `ui.settings.addCategory("@settings.editor", Icon.editor, ...)` containing:
   - `Title("@settings.mu_mods", "@settings.mu_mods.info")`: for each
     `MU.modules` entry a `checkPref(module.name, module.def, b -> ui.showInfo("@settings.mu_restart"))`
     - toggling shows the restart notice, both directions, no revert.
     `module.name`/`module.def` are the single source of truth (Q1).
   - `Title("@settings.rules_dialog")`: `checkPref("mu_hidden_rules", true)`,
     `checkPref("mu_env_settings", true)` - feature toggles read by
     RulesDialogModule, instant effect (no restart), string literals.
3. Register the category on `ClientLoadEvent` **after** `MU.modules` is
   filled and **before** the module init loop (the category builder reads the
   registry synchronously - registering too early silently yields an empty
   module section).
4. Setting display labels: v9's `Setting(String)` constructor resolves the title
   as `bundle.get("setting." + name + ".name", name)` - the bundle key shape is
   therefore `setting.<name>.name`, falling back to the raw key. Ship
   `setting.mu_hidden_rules.name`, `setting.mu_env_settings.name`, and
   `setting.<moduleKey>.name` for every module (currently
   `setting.mu_rules_dialog.name`) so no raw keys show. Vanilla bundle has
   `category.general` already - do not redefine it.

## Acceptance criteria

- The settings menu shows the new category with two titled sections.
- Toggling any module row (from `MU.modules`) shows the restart notice dialog
  and persists the value; the module list means future modules appear
  automatically.
- `mu_hidden_rules` / `mu_env_settings` rows appear with default true when the key
  is unset; values persist across restarts.
- No raw untranslated keys are visible in the category (every label resolves via
  `setting.<name>.name` or `settings.*`).

## Constraints

- Settings dialog modification is NOT a module - always enabled, no toggle for it.
- mu_-prefixed keys (Q5), English bundle only (Q9), explicit default on every
  `settings.getBool`/`checkPref` call.
- String literals, no constants holder class (Q6) - the literal in
  `checkPref("mu_hidden_rules", true)` and the one in RulesDialogModule must
  match exactly; acceptance relies on grep.
- `checkPref` already registers `settings.defaults(name, def)` synchronously at
  registration time in v9, but code must still pass defaults explicitly
  (`settings.getBool` may be called before the category ever built, e.g. by
  module init).

## Refs

- `old-src/java/mu/mods/SettingsDialogMod.java` - intent, Title class, layout.
- v9 `/root/projects/Mindustry/core/src/mindustry/ui/dialogs/SettingsMenuDialog.java` -
  `addCategory(String, Drawable, Cons<SettingsTable>)`, `checkPref(String, boolean, Boolc)`,
  `SettingsTable.Setting` (`public String title`, `Setting(String)`, `add(SettingsTable)`).
- v9 `/root/projects/Mindustry/core/src/mindustry/core/UI.java` - `showInfo(String)`.
- `ai-artifacts/tasks/mu-module-framework/task.md` - `MU.modules` registry.
- `ai-artifacts/tasks/rules-dialog-module/task.md` - consumer of the feature toggles.

## Open questions

- None.
