package mu.mods;

import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static mindustry.Vars.ui;
import static mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;
import static mu.MUVars.*;

public class SettingsDialogMod{
    public static void enable(){
        ui.settings.addCategory("@settings.editor", Icon.editor, table -> {
            table.pref(new Title("@category.general"));
            table.checkPref("mu_check_for_updates", true);
            table.sliderPref("editor_content_buttons_size", 50, 30, 80, i -> i + "px");
            
            table.pref(new Title("@settings.mu_mods", "@settings.mu_mods.info"));
            table.checkPref("mu_editor_mod", true, b -> editorMod.update());
            table.checkPref("mu_rules_mod", true, b -> updateRulesMods());
            table.checkPref("mu_resize_mod", true, b -> resizeMod.update());
            
            table.pref(new Title("@settings.rules_dialog"));
            table.checkPref("editor_hidden_rules", true);
            table.checkPref("editor_revealed_blocks", true);
            table.checkPref("editor_planet_background", true);
            table.checkPref("editor_environment_settings", true);
            table.checkPref("editor_better_content_dialogs", true);
        });
    }

    // Not a setting but I want these
    private static class Title extends Setting{
        public String bottomText = "";

        public Title(String text, String bottomText){
            super("");
            this.title = text;
            this.bottomText = bottomText;
        }

        public Title(String text){
            super("");
            this.title = text;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(title).color(Pal.accent).padTop(20f).padRight(100f).padBottom(-3f).left().pad(5f);
            table.row();
            table.image().color(Pal.accent).height(3f).padRight(100f).left().fillX().padBottom(5f);
            table.row();
            if(!bottomText.equals("")){
                table.add(bottomText).color(Pal.lightishGray).padRight(100f).left().padBottom(5f);
                table.row();
            }
        }
    }
}
