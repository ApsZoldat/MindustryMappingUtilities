package mu.modifying.ui;

import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static mindustry.Vars.ui;
import static mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;

public class SettingsDialog{
    public static void modify(SettingsMenuDialog dialog){
        ui.settings.addCategory("@settings.editor", Icon.editor, table -> {
            table.pref(new Title("@settings.rules_dialog"));
            table.checkPref("editor_hidden_rules", true);
            table.checkPref("editor_rules_info", true);
            table.checkPref("editor_rules_search", true);
            table.checkPref("editor_better_content_dialogs", true);
            table.checkPref("editor_revealed_blocks", true);
        });
    }

    // Not a setting but I want these
    private static class Title extends Setting{
        public Title(String text){
            super("");
            this.title = text;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(title).color(Pal.accent).padTop(20).padRight(100f).padBottom(-3).left().pad(5);
            table.row();
            table.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20).left().fillX().padBottom(2f);
            table.row();
        }
    }
}
