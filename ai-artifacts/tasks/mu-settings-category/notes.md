# Notes - MU settings category

## What changed

Implemented the always-enabled settings category per `task.md`:

- **`java/mu/SettingsDialogMod.java` (new)** - static `enable()`, NOT a
  `MUModule` (always enabled, no toggle of its own):
  - `ui.settings.addCategory("@settings.editor", Icon.editor, ...)` (v9
    `SettingsMenuDialog.addCategory(String, Drawable, Cons<SettingsTable>)`).
  - `Title` inner class ported from old-src: a `SettingsTable.Setting` subclass
    rendering an accent-colored section header + underline + optional gray
    bottom text. `Setting("")` then overwrite `title`, so the Reset button's
    `settings.remove("")` is a no-op (same as old-src).
  - Module section: `Title("@settings.mu_mods", "@settings.mu_mods.info")` then,
    for each `MU.modules` entry, `checkPref(module.name, module.def,
    b -> ui.showInfo("@settings.mu_restart"))` - restart notice in both
    directions, no revert. `module.name`/`module.def` are the single source of
    truth (Q1).
  - Rules-dialog section: `Title("@settings.rules_dialog")` then
    `checkPref("mu_hidden_rules", true)` and `checkPref("mu_env_settings", true)`
    - string literals, explicit defaults, read by `RulesDialogModule`.
- **`java/mu/MU.java`** - `SettingsDialogMod.enable()` called on
  `ClientLoadEvent` **after** module registration and **before** the init loop
  (the `SettingsCategory` constructor runs the builder synchronously, so
  registering earlier silently yields an empty module section).
- **Test modules removed** (user-confirmed): the `test-fail`/`test-npe`/
  `test-off`/`test-ok` scratch modules from `mu-module-framework` were deleted.
  They would otherwise show as raw keys in the new category and pop the failure
  dialog on every startup. The init loop is unchanged and still exercised by the
  first real module (`rules-dialog-module`).
- **`assets/bundles/bundle.properties`** - replaced the stale pre-v9
  `setting.*` block with the new keys (English only, Q9):
  `settings.mu_restart` (new), `setting.mu_rules_dialog.name` (new, for the
  module row once `rules-dialog-module` registers), `setting.mu_hidden_rules.name`
  and `setting.mu_env_settings.name` (renamed from the old `editor_*` keys).
  Dropped dead keys `setting.mu_editor_mod.*`, `setting.mu_rules_mod.name`,
  `setting.mu_resize_mod.*`, `setting.editor_hidden_rules.name`,
  `setting.editor_environment_settings.name` (grep-verified: no references in
  `java/` or `ai-artifacts/`). `settings.editor`, `settings.mu_mods(.info)`,
  `settings.rules_dialog` were kept; `category.general` is not redefined.

## Changed files

- `java/mu/SettingsDialogMod.java` (new)
- `java/mu/MU.java` (enable() call + test module removal)
- `assets/bundles/bundle.properties` (settings keys)

## Build

`./gradlew build` green after the change (baseline was green before it).

## Human test checklist

1. Open Settings - a new "Editor Settings" category (editor icon) appears after
   "Dev". It opens to two sections: "Mapping Utilities Mods" and "Custom Rules
   Dialog", each with an accent header + underline; the mods section shows its
   gray `.info` line.
2. No raw keys anywhere in the category (labels resolve via `setting.<name>.name`
   or `settings.*`); the reset button still works and does not error.
3. With `rules-dialog-module` registered: toggling any module row shows the
   restart notice dialog and persists the value (Settings > Other > Mod Settings
   or the `settings.dat` file); the module list means a future `modules.add(...)`
   needs no settings-code change.
4. `mu_hidden_rules` / `mu_env_settings` rows show default true when the key is
   unset; toggling persists across restarts.
5. Restart-notice fires on both check and uncheck (both directions), and the
   value is NOT reverted by the notice.
6. The category is registered at the right time: with a module registered, its
   row is present immediately (not empty) - confirms `enable()` runs after
   `modules.add(...)`.
7. Startup with no modules: the settings category still builds (empty mods
   section), no crash, no failure dialog.
