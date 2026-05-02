package mu;

import arc.Events;
import arc.util.Reflect;
import arc.struct.*;
import mindustry.editor.MapInfoDialog;
import mindustry.editor.BannedContentDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.modifying.ResizeDialog;
import mu.modifying.RulesDialog;
import mu.modifying.SettingsDialog;
import mu.utils.UpdateChecker;

import static arc.Core.settings;
import static mindustry.Vars.ui;
import static mu.MUVars.*;

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


            Reflect.set(infoRules, "bannedBlocks", MUVars.betterBannedBlocks);
            Reflect.set(infoRules, "bannedUnits", MUVars.betterBannedUnits);
            Reflect.set(playRules, "bannedBlocks", MUVars.betterBannedBlocks);
            Reflect.set(playRules, "bannedUnits", MUVars.betterBannedUnits);
            Reflect.set(playtestRules, "bannedBlocks", MUVars.betterBannedBlocks);
            Reflect.set(playtestRules, "bannedUnits", MUVars.betterBannedUnits);

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
