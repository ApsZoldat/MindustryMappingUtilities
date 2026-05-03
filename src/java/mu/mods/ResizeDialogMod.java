package mu.mods;

import arc.scene.ui.layout.Table;
import mindustry.editor.MapResizeDialog;

public class ResizeDialogMod{
    public static void modify(MapResizeDialog dialog){
        MapResizeDialog.minSize = 1;
        MapResizeDialog.maxSize = 2147483647; // I don't care about you crashing your game
        dialog.shown(() -> setup(dialog));
    }

    public static void setup(MapResizeDialog dialog){
        ((Table)dialog.cont.getChildren().get(0)).getCells().each(c -> c.maxTextLength(10));
    }
}
