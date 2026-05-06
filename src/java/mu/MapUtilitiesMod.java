package mu;

import arc.Events;
import arc.util.*;
import arc.struct.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import mu.editor.*;
import mu.mods.*;
import mu.utils.*;

import static arc.Core.settings;
import static mu.MUVars.*;

public class MapUtilitiesMod extends Mod{
    public MapUtilitiesMod(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            SettingsDialogMod.enable();

            editor = new MUMapEditor();
            editorDialog = new MUMapEditorDialog();

            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(Vars.ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.editor, "playtestDialog"), "dialog");
            CustomRulesDialog newRules = Reflect.get(MapInfoDialog.class, Reflect.get(MapEditorDialog.class, editorDialog, "infoDialog"), "ruleInfo");

            allMods = new Seq<MUMod>();

            // Rules mods
            allMods.add(new RulesDialogMod(infoRules));
            allMods.add(new RulesDialogMod(playRules));
            allMods.add(new RulesDialogMod(playtestRules));
            allMods.add(new RulesDialogMod(newRules));

            // Resize mods
            allMods.add(new ResizeDialogMod(Reflect.get(Vars.ui.editor, "resizeDialog")));
            allMods.add(new ResizeDialogMod(Reflect.get(MapEditorDialog.class, editorDialog, "resizeDialog")));

            // Editor mod
            allMods.add(new EditorDialogMod(Vars.ui.editor, Vars.editor));

            updateMods();

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
