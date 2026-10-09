package mu;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import mu.modules.*;

import static mindustry.Vars.*;

/**
 * Adds the always-enabled "Editor Settings" category to the vanilla settings menu.
 * Not a {@link MUModule}: it cannot be toggled off, so it has no module row of its own.
 */
public class SettingsDialogMod{
    public static void enable(){
        ui.settings.addCategory("@settings.editor", Icon.editor, table -> {
            table.pref(new Title("@settings.mu_modules", "@settings.mu_modules.info"));

            for(MUModule module : MU.modules){
                table.checkPref(module.name, module.def, b -> ui.showInfo("@settings.mu_restart"));
            }

            table.pref(new Title("@settings.rules_dialog"));
            table.checkPref("mu_hidden_rules", true);
            table.checkPref("mu_env_settings", true);
        });
    }

    /** A section header row in a settings table; not an actual setting, so it is never reset or persisted. */
    private static class Title extends Setting{
        public String bottomText = "";

        public Title(String text, String bottomText){
            super("");
            this.title = text;
            this.bottomText = bottomText;
        }

        public Title(String text){
            this(text, "");
        }

        @Override
        public void add(SettingsTable table){
            table.add(title).color(Pal.accent).padTop(20f).padRight(100f).padBottom(-3f).left().pad(5f);
            table.row();
            table.image().color(Pal.accent).height(3f).padRight(100f).left().fillX().padBottom(5f);
            table.row();
            if(!bottomText.isEmpty()){
                table.add(bottomText).color(Pal.lightishGray).padRight(100f).left().padBottom(5f);
                table.row();
            }
        }
    }
}
