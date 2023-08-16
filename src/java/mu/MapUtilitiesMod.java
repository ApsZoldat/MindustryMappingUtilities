package mu;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.MapPlayDialog;
import mu.reflect.ui.RulesDialog;
import mu.reflect.ui.SettingsDialog;

import static mindustry.Vars.ui;

public class MapUtilitiesMod extends Mod {
    public MapUtilitiesMod() {
        Events.on(EventType.ClientLoadEvent.class, e -> {
            MapInfoDialog infoDialog = Reflect.get(ui.editor, "infoDialog");
            RulesDialog.change(Reflect.get(infoDialog, "ruleInfo"));
            MapPlayDialog playDialog = Reflect.get(ui.custom, "dialog");
            RulesDialog.change(Reflect.get(playDialog, "dialog"));

            SettingsDialog.change(ui.settings);
        });
    }
}
