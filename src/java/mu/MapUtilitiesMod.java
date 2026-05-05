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

            editorMod = new EditorDialogMod(Vars.ui.editor, Vars.editor);
            editorMod.update();

            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(Vars.ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.editor, "playtestDialog"), "dialog");

            infoRulesMod = new RulesDialogMod(infoRules);
            playRulesMod = new RulesDialogMod(playRules);
            playtestRulesMod = new RulesDialogMod(playtestRules);

            updateRulesMods();

            resizeMod = new ResizeDialogMod(Reflect.get(Vars.ui.editor, "resizeDialog"));
            resizeMod.update();

            if(settings.getBool("mu_check_for_updates")) UpdateChecker.run();
        });
    }
}
