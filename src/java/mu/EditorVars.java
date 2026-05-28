package mu;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mu.mods.*;
import mu.editor.*;
import mu.ui.*;
import mu.ui.data.*;

public class EditorVars{
    // All package names
    public static Seq<String> packageNames = Seq.with("mu", "mu.mods", "mu.utils", "mu.editor", "mu.editor.modes", "mu.editor.brushes", "mu.ui", "mu.ui.data", "mu.ui.dialogs");

    // New editor
    // TODO: consider renaming these
    public static MUMapEditor editor;
    public static MUMapView editorView;
    public static MUMapEditorDialog editorDialog;
    public static EditorUI editorUi;
    public static EditorState editorState;

    // Editor UI Data
    

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
