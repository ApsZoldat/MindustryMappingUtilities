package mu;

import arc.Events;
import arc.util.Reflect;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.modifying.ui.ResizeDialog;
import mu.modifying.ui.RulesDialog;
import mu.modifying.ui.SettingsDialog;
import mu.utils.UpdateChecker;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.editor, "playtestDialog"), "dialog");

            RulesDialog.modify(infoRules);
            RulesDialog.modify(playRules);
            RulesDialog.modify(playtestRules);

            ResizeDialog.modify(Reflect.get(ui.editor, "resizeDialog"));
            SettingsDialog.modify();

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
