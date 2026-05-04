package mu.mods;

import arc.util.*;
import mindustry.gen.*;
import mindustry.editor.*;
import mindustry.ui.dialogs.*;

import static arc.Core.settings;

public class EditorDialogMod{
    public MapEditorDialog dialog;
    
    public boolean isEnabled;
    
    public EditorDialogMod(MapEditorDialog dialog){
        this.dialog = dialog;
        isEnabled = false;
    }
    
    public void update(){
        if(settings.getBool("mu_editor_mod") && !isEnabled){
            enable();
        }
    }
    
    public void enable(){
        BaseDialog menu = Reflect.get(dialog, "menu");
        
        menu.cont.row();
        menu.cont.button("@mu_editor", Icon.wrench, () -> {
            Reflect.invoke(dialog, "tryExit");
            menu.hide();
        }).padTop(1).size(180f * 2f + 10, 60f);

        isEnabled = true;
    }
}