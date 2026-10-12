| ID | AC | Preconditions | Steps (high-level) | Expected | Result |
|----|----|---------------|---------------------|----------|--------|
| CH1 | AC1 | Mod is installed, game is running | Check file `java/mu/ui/dialogs/MUSettingsDialog.java` exists and extends `BaseDialog` | File exists, extends `BaseDialog` | |
| CH2 | AC1 | Open the settings dialog | Verify checkboxes exist for each module in `MU.modules` and for `mu_hidden_rules` and `mu_env_settings` | All expected checkboxes present | |
| CH3 | AC2 | Open the settings dialog | Check the dialog title | Title reads the localized `@settings.mapping_utilities` | |
| CH4 | AC3 | Open `MUSettingsDialog` | Toggle a module checkbox on or off | Restart info message (`@settings.mu_restart`) appears | |
| CH5 | AC4 | Change a setting, exit game, restart | Reopen `MUSettingsDialog` and check checkbox states | Changed settings are preserved from previous session | |
| CH6 | AC5 | Check filesystem | Verify `java/mu/SettingsDialogMod.java` does not exist | File is absent | |
| CH7 | AC6 | Open the settings menu | Look for a button with editor icon and `mapping_utilities` label | Button is visible in the settings menu | |
| CH8 | AC7 | Settings menu is open | Click the `mapping_utilities` button | `MUSettingsDialog` opens | |
| CH9 | AC7 | `MUSettingsDialog` is open | Close it (Escape or close button), click the button again | Dialog opens again without errors | |
| CH10 | AC7 | `MUSettingsDialog` is open | Resize the game window | Dialog remains usable and does not break layout | |
| CH11 | AC1 | Check game logs | Perform CH2 through CH10 | No errors in the log related to the mod | |
