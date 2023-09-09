package mu;

import arc.Events;
import arc.util.Reflect;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.modifying.ui.RulesDialog;
import mu.modifying.ui.SettingsDialog;
import mu.utils.UpdateChecker;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod() {
        Events.on(EventType.ClientLoadEvent.class, e -> {
            MapInfoDialog infoDialog = Reflect.get(ui.editor, "infoDialog");
            RulesDialog.modify(Reflect.get(infoDialog, "ruleInfo"));
            MapPlayDialog playDialog = Reflect.get(ui.custom, "dialog");
            RulesDialog.modify(Reflect.get(playDialog, "dialog"));

            SettingsDialog.modify();

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
