package mu.reflect.ui;

import mindustry.gen.Icon;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static mindustry.Vars.ui;

public class SettingsDialog {
    public static void change(SettingsMenuDialog dialog){
        ui.settings.addCategory("@settings.editor", Icon.editor, table -> {
            table.checkPref("editor_hidden_rules", true);
            table.checkPref("editor_rules_info", true);
            table.checkPref("editor_rules_search", true);
        });
    }

}
