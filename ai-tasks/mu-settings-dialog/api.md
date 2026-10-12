# API of new classes: mu-settings-dialog

## Dependencies and creation order
- `MU` -> `MUSettingsDialog` (creates instance, adds button to settings menu)

## Classes

### java/mu/ui/dialogs/MUSettingsDialog.java
Responsibility: Custom dialog containing the mod's settings (module toggles + rules dialog settings).
```java
package mu.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class MUSettingsDialog extends BaseDialog{
    /** Contract: called once by MU.java after ClientLoadEvent. Sets title, adds close button, registers build() as the shown() callback. */
    public MUSettingsDialog(){
        super("@settings.mapping_utilities");
        addCloseButton();
        shown(this::build);
    }

    /** Contract: called every time the dialog is shown. Clears cont and rebuilds all checkboxes from current Core.settings values. */
    void build(){
        // Clear cont and rebuild checkboxes for each module in MU.modules
        // Build checkboxes for mu_hidden_rules and mu_env_settings
    }
}
```

### java/mu/MU.java (changes)
Responsibility: Mod entry point. Creates the MUSettingsDialog singleton and adds a button to the settings menu.
```java
package mu;

// ... existing imports ...
import mu.ui.dialogs.*;

public class MU extends Mod{
    public static Seq<MUModule> modules = new Seq<>();
    /** Singleton instance of the settings dialog; created once at startup. */
    public static MUSettingsDialog settingsDialog;

    public MU(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            // ... existing module registration ...

            // Create the settings dialog singleton
            settingsDialog = new MUSettingsDialog();

            // Use reflection to access ui.settings.menu and add a button
            // Button: Icon.editor, label "@settings.mapping_utilities", click -> settingsDialog.show()

            // ... existing init loop ...
        });
    }
}
```

## Coverage
| AC | Members |
|----|---------|
| AC1 | `build()` — builds checkboxes for modules and rules settings |
| AC2 | `MUSettingsDialog()` constructor — calls `super("@settings.mapping_utilities")` |
| AC3 | `build()` — module checkbox listener shows restart message |
| AC4 | `build()` — uses `Core.settings.getBool/put` for persistence |
| AC5 | `SettingsDialogMod.java` deleted (no new member) |
| AC6 | `MU()` — reflection code accessing `ui.settings.menu` |
| AC7 | `MU()` — button click calls `settingsDialog.show()` |
