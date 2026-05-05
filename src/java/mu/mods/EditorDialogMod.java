package mu.mods;

import arc.util.*;
import mindustry.gen.*;
import mindustry.editor.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import mu.editor.*;
import mu.MUVars;

import static arc.Core.settings;

public class EditorDialogMod{
    public MapEditorDialog oldDialog;
    public MapEditor oldEditor;

    public MUMapEditorDialog newDialog;

    public EditorDialogMod(MapEditorDialog dialog, MapEditor editor){
        oldDialog = dialog;
        oldEditor = editor;
        
        newDialog = new MUMapEditorDialog();
    }
    
    public void update(){
        if(settings.getBool("mu_editor_mod")){
            enable();
        }else{
            disable();
        }
    }
    
    public void enable(){
        Vars.ui.editor = newDialog;
        Vars.editor = MUVars.editor;
    }

    public void disable(){
        Vars.ui.editor = oldDialog;
        Vars.editor = oldEditor;
    }
}