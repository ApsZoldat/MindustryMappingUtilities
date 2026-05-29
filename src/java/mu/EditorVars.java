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
    public static MUMapEditor editor;
    public static MUMapView view;
    public static EditorUI ui;
    public static EditorState state;
    public static MUMapEditorDialog dialog;

    // UI mods
    public static Seq<MUMod> allMods;

    public static void init(){
        editor = new MUMapEditor();
        view = new MUMapView();
        ui = new EditorUI();
        state = new EditorState();
        dialog = new MUMapEditorDialog();
    }

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
