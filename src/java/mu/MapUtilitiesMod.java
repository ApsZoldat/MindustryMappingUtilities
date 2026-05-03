package mu;

import arc.Events;
import arc.util.*;
import arc.struct.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mu.mods.*;
import mu.utils.*;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static mu.MUVars.*;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(ui.editor, "playtestDialog"), "dialog");

            RulesDialogMod.modify(infoRules);
            RulesDialogMod.modify(playRules);
            RulesDialogMod.modify(playtestRules);

            ResizeDialogMod.modify(Reflect.get(ui.editor, "resizeDialog"));
            SettingsDialogMod.modify();


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
