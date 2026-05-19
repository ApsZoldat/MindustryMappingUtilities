package mu.mods;

import arc.util.*;
import mindustry.gen.*;
import mindustry.editor.*;
import mindustry.ui.dialogs.*;
import mindustry.Vars;
import mu.editor.*;
import mu.EditorVars;

import static arc.Core.settings;

public class EditorDialogMod extends MUMod{
    public MapEditorDialog oldDialog;
    public MapEditor oldEditor;

    public EditorDialogMod(MapEditorDialog dialog, MapEditor editor){
        this.settingName = "mu_editor_mod";
        oldDialog = dialog;
        oldEditor = editor;
    }

    @Override
    public void enable(){
        Vars.ui.editor = EditorVars.editorDialog;
        Vars.editor = EditorVars.editor;
    }

    @Override
    public void disable(){
        Vars.ui.editor = oldDialog;
        Vars.editor = oldEditor;
    }
}