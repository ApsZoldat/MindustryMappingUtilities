package mu.ui.dialogs;

import arc.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;
import mu.MU;
import mu.modules.*;

import static mindustry.Vars.*;

public class MUSettingsDialog extends BaseDialog{
    public MUSettingsDialog(){
        super("@mu_settings");
        addCloseButton();
        shown(this::build);
    }

    /** Clears cont and rebuilds all checkboxes from current Core.settings values. */
    void build(){
        cont.clearChildren();
        cont.top();

        cont.add("@settings.mu_modules").color(Pal.accent).padTop(20f).left();
        cont.row();
        cont.image().color(Pal.accent).height(3f).fillX().padBottom(5f);
        cont.row();
        cont.add("@settings.mu_modules.info").color(Pal.lightishGray).fillX().padBottom(5f).left().wrap();
        cont.row();

        for(MUModule module : MU.modules){
            cont.check(Core.bundle.get("setting." + module.name + ".name", module.name), Core.settings.getBool(module.name, module.def), b -> {
                Core.settings.put(module.name, b);
                ui.showInfo("@settings.mu_restart");
            }).left().padTop(7f);
            cont.row();
        }

        cont.add("@settings.rules_dialog").color(Pal.accent).padTop(20f).left();
        cont.row();
        cont.image().color(Pal.accent).height(3f).fillX().padBottom(5f);
        cont.row();

        cont.check(Core.bundle.get("setting.mu_hidden_rules.name", "mu_hidden_rules"), Core.settings.getBool("mu_hidden_rules", true), b -> Core.settings.put("mu_hidden_rules", b)).left().padTop(7f);
        cont.row();
        cont.check(Core.bundle.get("setting.mu_env_settings.name", "mu_env_settings"), Core.settings.getBool("mu_env_settings", true), b -> Core.settings.put("mu_env_settings", b)).left().padTop(7f);
    }
}
