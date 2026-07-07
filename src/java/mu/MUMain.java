package mu;

import arc.Events;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.io.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import mu.editor.*;
import mu.mods.*;
import mu.utils.*;
import mu.ui.*;
import mu.ui.data.*;

import static arc.Core.settings;
import static mindustry.Vars.mods;
import static mu.EditorVars.*;

public class MUMain extends Mod{
    public MUMain(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            SettingsDialogMod.enable();

            EditorVars.init();

            // Mods
            allMods = new Seq<MUMod>();

            CustomRulesDialog infoRules = Reflect.get(MapInfoDialog.class, Reflect.get(Vars.ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog playRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtestRules = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.editor, "playtestDialog"), "dialog");
            CustomRulesDialog newRules = Reflect.get(MapInfoDialog.class, Reflect.get(MapEditorDialog.class, dialog, "infoDialog"), "ruleInfo");

            // Rules mods
            allMods.add(new RulesDialogMod(infoRules));
            allMods.add(new RulesDialogMod(playRules));
            allMods.add(new RulesDialogMod(playtestRules));
            allMods.add(new RulesDialogMod(newRules));

            // Resize mods
            allMods.add(new ResizeDialogMod(Reflect.get(Vars.ui.editor, "resizeDialog")));
            allMods.add(new ResizeDialogMod(Reflect.get(MapEditorDialog.class, dialog, "resizeDialog")));

            // Editor mod
            // allMods.add(new EditorDialogMod(Vars.ui.editor, Vars.editor));

            updateMods();

            if(settings.getBool("mu_check_for_updates", false)) UpdateChecker.run();

            // Subtitle randomizing
            try{
                LoadedMod mod = mods.getMod("mapping-utilities");
                SubtitleRandomizer randomizer = new SubtitleRandomizer(mod);
                randomizer.removeMaxLength();
                randomizer.fetchSubtitles();
                randomizer.randomize();
            }catch (Exception err){
                Log.err("Oops, failed to randomize Mapping Utilities subtitle, how unfortunate!", err);
            }

            // Importing all packages to Rhino JS
            jsManager.importPackages();

            // Adding all class tags
            // MUJson.classTags(Reflect.get(Json.class, JsonIO.json, "tagToClass"));
            // MUJson.classTags(classTags);
        });
    }
}
