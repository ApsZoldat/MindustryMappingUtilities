# Task: mu-settings-dialog
Status: implemented
Mindustry commit: 5d45d9ad95

## Goal
Create `java/mu/ui/dialogs/MUSettingsDialog.java` — a custom `BaseDialog` containing the settings UI currently built inline in `java/mu/SettingsDialogMod.java`. Delete `SettingsDialogMod.java`. In `MU.java`, add a button to the settings menu that opens this dialog.

## Non-goals
- Porting missing old-src classes (`EditorVars`, `RulesDialogMod`, `ResizeDialogMod`) — out of scope
- Changing the settings content or adding new settings — out of scope
- The `Title` class — not needed in a custom dialog (it was a fake `Setting` for the vanilla settings table)

## Requirements
- R1 [decision] `java/mu/ui/dialogs/MUSettingsDialog.java` exists and contains the settings UI currently in `SettingsDialogMod.java` (module toggles, rules dialog settings)
- R2 [decision] `java/mu/SettingsDialogMod.java` is deleted
- R3 [decision] `MU.java` adds a button to the settings menu that opens `MUSettingsDialog`

## Acceptance criteria
- AC1 (R1) `MUSettingsDialog.java` exists at `java/mu/ui/dialogs/`, extends `BaseDialog`, and contains checkboxes for each `MUModule` in `MU.modules` plus `mu_hidden_rules` and `mu_env_settings`
- AC2 (R1) The dialog title is `"@settings.mapping_utilities"`
- AC3 (R1) Toggling a module checkbox shows the restart info message (`"@settings.mu_restart"`)
- AC4 (R1) Settings persist across sessions via `Core.settings`
- AC5 (R2) `java/mu/SettingsDialogMod.java` no longer exists
- AC6 (R3) `MU.java` contains reflection code that accesses `ui.settings.menu` and adds a button with `Icon.editor` and label `"@settings.mapping_utilities"`
- AC7 (R3) Clicking the button opens `MUSettingsDialog`

## Decisions log
| # | Question | Decision |
|---|----------|----------|
| 1 | What to copy? | Current `java/mu/SettingsDialogMod.java`, not old-src |
| 2 | What happens to current `SettingsDialogMod.java`? | Delete it |
| 3 | How to hook into settings? | Create the button in `MU.java`, no `enable()` method |
| 4 | How to add button to settings menu? | Reflection on private `menu` field |
| 5 | What does `MUSettingsDialog` extend? | `BaseDialog` |
| 6 | What about the `Title` class? | Drop it — not needed in a custom dialog |
| 7 | How to build settings UI? | Manual with Arc `CheckBox`/`Slider` + `Core.settings` |
| 8 | Button icon and label? | `Icon.editor` + `"@settings.mapping_utilities"` |
| 9 | Dialog title? | `"@settings.mapping_utilities"` |
| 10 | What settings in the dialog? | Both module toggles and rules dialog settings |
| 11 | How to show the dialog? | Singleton — create once in `MU.java`, call `show()` on same instance |
| 12 | When to run reflection? | Once, in `ClientLoadEvent` handler |
| 13 | What methods on `MUSettingsDialog`? | Constructor registers `shown(this::build)`; `build()` method does the UI |
| 14 | Where to store the singleton? | Field in `MU.java` |
| 15 | Reflection code placement? | Inline in `ClientLoadEvent` handler |
| 16 | Where to build UI? | `build()` method called via `shown(this::build)` in constructor |

## Deviations
| # | Member or AC | Spec said | Implemented | Reason |
|---|--------------|-----------|-------------|--------|
| D1 | `MUSettingsDialog` imports | api.md fixed the import list (`arc.*`, `arc.scene.ui.*`, `arc.scene.ui.layout.*`, `mindustry.gen.*`, `mindustry.ui.dialogs.*`, `static mindustry.Vars.*`) | added `mindustry.graphics.Pal`, `mu.MU`, `mu.modules.*` | Compiler-forced: `Pal` lives in `mindustry.graphics` (not `arc.graphics`); `build()` references `MU.modules`/`MUModule`, which the listed imports do not cover. No signature or behavior change. |
| D2 | `MU.java` imports | api.md sketch implied existing imports cover `ui`, `Table`, `Icon`, `TextButton` | added `arc.scene.ui.TextButton`; `Table`/`Icon` resolved via wildcard imports (`arc.scene.ui.layout.*`, `mindustry.gen.*`); `ui` referenced as `Vars.ui`, matching the file's existing fully-qualified style | Compiler-forced: none of these symbols were in scope. No signature or behavior change. |

## Open questions and assumptions
- A1 [assumption] `MUSettingsDialog.build()` section headers: plain accent-colored label + divider rows with `fillX()`, replacing the old `Title` class rendering (decision 6 drops `Title`). Width is not clamped to `min(500f, width/1.2/Scl)` as in `Title` — cosmetic only, no behavioral difference.
- A2 [assumption] `MU.java` button survival: reflection grabs `menu` once; a `menu.update(...)` callback re-adds the button whenever it is missing. Forced by engine behavior: `SettingsMenuDialog.rebuildMenu()` calls `menu.clearChildren()` on every settings open AND on back-from-category, so a one-shot add would make the button vanish (breaking AC7/CH7). The per-frame check allocates nothing.
