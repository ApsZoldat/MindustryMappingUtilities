package mu;

import arc.Events;
import arc.util.Reflect;
import mindustry.core.Version;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.legacy.modifying.ui.LegacyRulesDialog;
import mu.legacy.modifying.ui.LegacySettingsDialog;
import mu.utils.UpdateChecker;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod() {
        Events.on(EventType.ClientLoadEvent.class, e -> {
            MapInfoDialog infoDialog = Reflect.get(ui.editor, "infoDialog");
            MapPlayDialog playDialog = Reflect.get(ui.custom, "dialog");
            MapPlayDialog playtestDialog = Reflect.get(ui.editor, "playtestDialog");


            if(0 <= Version.build && Version.build <= 146){
                LegacyRulesDialog.modify(Reflect.get(infoDialog, "ruleInfo"));
                LegacyRulesDialog.modify(Reflect.get(playDialog, "dialog"));
                LegacyRulesDialog.modify(Reflect.get(playtestDialog, "dialog"));
                LegacySettingsDialog.modify();
            }else{

            }

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
