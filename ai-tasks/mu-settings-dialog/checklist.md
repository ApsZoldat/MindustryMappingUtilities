| ID | AC | Preconditions | Steps (high-level) | Expected | Result |
|----|----|---------------|---------------------|----------|--------|
| CH1 | AC1 | Mod is installed, game is running | Check file `java/mu/ui/dialogs/MUSettingsDialog.java` exists and extends `BaseDialog` | File exists, extends `BaseDialog` | |
| CH2 | AC1 | Game running with the mod installed (build with `./gradlew jar`, install `build/libs/MindustryMappingUtilitiesDesktop.jar` as a mod) | Open the settings menu (Esc → Settings), click the "Mapping Utilities" button (editor icon, last entry of the menu list) | Dialog shows checkboxes: "Rules Dialog Improvements", "Enable Hidden Rules", "Add Environment Settings" | |
| CH3 | AC2 | `MUSettingsDialog` is open (from CH2) | Look at the dialog title | Title reads "Mapping Utilities" | |
| CH4 | AC3 | `MUSettingsDialog` is open | Toggle "Rules Dialog Improvements" on or off | Info message "This change will only take effect after restarting the game." appears | |
| CH5 | AC4 | Changed a setting in CH4 | Exit the game, relaunch, reopen Settings → Mapping Utilities | Checkbox states match what was set before exit | |
| CH6 | AC5 | Check filesystem | Verify `java/mu/SettingsDialogMod.java` does not exist | File is absent | |
| CH7 | AC6 | In game | Open the settings menu (Esc → Settings) | Button with editor icon and "Mapping Utilities" label is visible as the last entry of the menu list | |
| CH8 | AC7 | Settings menu is open | Click the "Mapping Utilities" button | `MUSettingsDialog` opens with title "Mapping Utilities" | |
| CH9 | AC7 | `MUSettingsDialog` is open | Close it (Escape or the close button at the bottom), click the button again | Dialog opens again without errors | |
| CH10 | AC7 | Settings menu is open | Click into another settings category (e.g. Game), then click the back button | The "Mapping Utilities" button is still present in the menu list | |
| CH11 | AC7 | `MUSettingsDialog` is open | Resize the game window | Dialog remains usable, checkboxes still toggle correctly | |
| CH12 | AC1 | Performed CH2 through CH11 | Check the game log for mod output | No errors related to the mod; expected info lines are prefixed `[MU] `; if reflection fails, the log contains `[MU] Failed to add Mapping Utilities button to the settings menu` | |
| CH13 | AC3 | `MUSettingsDialog` is open | Toggle "Enable Hidden Rules" or "Add Environment Settings" | No restart message appears (matches old behavior: only module toggles show it) | |
