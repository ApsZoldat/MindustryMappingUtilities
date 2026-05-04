package mu;

import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mu.mods.*;
import mu.editor.*;

public class MUVars{
    // New editor
    public static MUMapEditor editor;

    // UI mods
    public static EditorDialogMod editorMod;

    public static RulesDialogMod infoRulesMod;
    public static RulesDialogMod playRulesMod;
    public static RulesDialogMod playtestRulesMod;

    public static ResizeDialogMod resizeMod;
    
    public static void updateRulesMods(){
        infoRulesMod.update();
        playRulesMod.update();
        playtestRulesMod.update();
    }
}
