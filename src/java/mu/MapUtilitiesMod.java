package mu;

import arc.Events;
import arc.util.Reflect;
import mindustry.core.Version;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.legacy.modifying.ui.LegacyRulesDialog;
import mu.legacy.modifying.ui.LegacySettingsDialog;
import mu.modifying.ui.ResizeDialog;
import mu.modifying.ui.RulesDialog;
import mu.utils.UpdateChecker;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.editor, "playtestDialog"), "dialog");

            if(0 <= Version.build && Version.build <= 146){
                LegacyRulesDialog.modify(infoRules);
                LegacyRulesDialog.modify(playRules);
                LegacyRulesDialog.modify(playtestRules);
            }else{
                RulesDialog.modify(infoRules);
                RulesDialog.modify(playRules);
                RulesDialog.modify(playtestRules);
            }
            ResizeDialog.modify(Reflect.get(ui.editor, "resizeDialog"));
            LegacySettingsDialog.modify();

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
