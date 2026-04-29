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
import mu.modifying.ui.BanDialog;
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

            Seq<BannedContentDialog> bannedContents = new Seq<>();
            
            bannedContents.add((BannedContentDialog)Reflect.get(infoRules, "bannedBlocks"));
            bannedContents.add((BannedContentDialog)Reflect.get(infoRules, "bannedUnits"));
            bannedContents.add((BannedContentDialog)Reflect.get(playRules, "bannedBlocks"));
            bannedContents.add((BannedContentDialog)Reflect.get(playRules, "bannedUnits"));
            bannedContents.add((BannedContentDialog)Reflect.get(playtestRules, "bannedBlocks"));
            bannedContents.add((BannedContentDialog)Reflect.get(playtestRules, "bannedUnits"));

            bannedContents.each(d -> {BanDialog.modify(d);});
            
            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
