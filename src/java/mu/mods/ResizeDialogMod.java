package mu.mods;

import arc.scene.event.*;
import arc.scene.ui.layout.Table;
import mindustry.editor.MapResizeDialog;

import static arc.Core.settings;

public class ResizeDialogMod extends MUMod{
    public MapResizeDialog dialog;
    public VisibilityListener shownListener;
    
    public int oldMinSize;
    public int oldMaxSize;
    
    public ResizeDialogMod(MapResizeDialog dialog){
        this.settingName = "mu_resize_mod";
        this.dialog = dialog;
        
        shownListener = new VisibilityListener(){
            @Override
            public boolean shown(){
                setup();
                return false;
            }
        };
        
        oldMinSize = dialog.minSize;
        oldMaxSize = dialog.maxSize;
    }

    @Override
    public void enable(){
        MapResizeDialog.minSize = 1;
        MapResizeDialog.maxSize = Integer.MAX_VALUE; // I don't care about you crashing your game
        dialog.addListener(shownListener);
    }

    @Override
    public void disable(){
        MapResizeDialog.minSize = oldMinSize;
        MapResizeDialog.maxSize = oldMaxSize;
        dialog.removeListener(shownListener);
    }

    public void setup(){
        ((Table)dialog.cont.getChildren().get(0)).getCells().each(c -> c.maxTextLength(10));
    }
}
