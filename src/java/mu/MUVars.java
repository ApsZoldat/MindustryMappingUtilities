package mu;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mu.mods.*;
import mu.editor.*;

public class MUVars{
    // New editor
    public static MUMapEditor editor;
    public static MUMapEditorDialog editorDialog;

    // UI mods
    public static Seq<MUMod> allMods;

    public static void updateMods(){
        allMods.each(m -> m.update());
    }
    
    public static void updateMods(Class cls){
        allMods.each(m -> {
            if(cls.isInstance(m)){
                m.update();
            }
        });
    }
}
