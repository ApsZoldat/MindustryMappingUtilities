package mu;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.Vars;
import mu.mods.*;
import mu.editor.*;
import mu.editor.blocks.*;
import mu.editor.blocks.BlocksMode.*;
import mu.editor.blocks.tools.*;
import mu.editor.blocks.brushes.*;
import mu.ui.*;
import mu.ui.data.*;
import mu.utils.*;

public class EditorVars{
    // All package names
    public static Seq<String> packageNames = Seq.with("mu", "mu.mods", "mu.utils", "mu.editor", "mu.editor.blocks", "mu.editor.blocks.tools", "mu.editor.blocks.brushes", "mu.ui", "mu.ui.data", "mu.ui.dialogs");

    // All JSON class tags
    public static ObjectMap<String, Class<?>> classTags = ObjectMap.of(
        "MUMapEditor", MUMapEditor.class,
        "MUEditorUI", EditorUI.class,
        "MUMapView", MUMapView.class,
        "MUEditorState", EditorState.class,
        "MUWindowData", WindowData.class,
        "MUTableData", TableData.class,
        "MUCellData", CellData.class,
        "MUButtonData", ButtonData.class,
        "MUNavigationMode", NavigationMode.class,
        "MUBlocksMode", BlocksMode.class,
        "MUBlocksPickTool", BlocksPickTool.class,
        "MUBlocksBrushTool", BlocksBrushTool.class,
        "MURectBrush", RectBrush.class,
        "MUBlocksSelectAction", BlocksSelectAction.class,
        "MUBlocksDeselectAction", BlocksDeselectAction.class,
        "MUBlocksDrawAction", BlocksDrawAction.class
    );  // TODO: ?

    // New editor
    public static MUMapEditor editor;
    public static MUMapView view;
    public static EditorUI ui;
    public static EditorState state;
    public static MUMapEditorDialog dialog;

    // JS Manager
    public static JSManager jsManager;

    // UI mods
    public static Seq<MUMod> allMods;

    public static void init(){
        editor = new MUMapEditor();
        view = new MUMapView();
        ui = new EditorUI();
        state = new EditorState();
        dialog = new MUMapEditorDialog();
        jsManager = new JSManager(Vars.mods.getScripts());
        
        // Mark these as singletons so they don't get copied on deserialization
        MUJson.addSingleton(editor);
        MUJson.addSingleton(view);
        MUJson.addSingleton(ui);
        MUJson.addSingleton(state);
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
