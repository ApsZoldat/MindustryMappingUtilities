package mu.modifying.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.editor.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;

import static arc.Core.settings;
import static mindustry.Vars.*;

public class BanDialog{
    private static ObjectMap<ContentType, Class> contentClasses = new ObjectMap<>();
    
    static {
        contentClasses.put(ContentType.block, Block.class);
        contentClasses.put(ContentType.unit, UnitType.class);
    }

    public static void modify(BannedContentDialog dialog){
        dialog.update(() -> {checkTables(dialog);});
    }

    private static void checkTables(BannedContentDialog dialog){
        if(dialog.isShown() && settings.getBool("editor_better_content_dialogs")){
            Table selectedTable = Reflect.get(dialog, "selectedTable");
            Table deselectedTable = Reflect.get(dialog, "deselectedTable");
            rebuildTable(dialog, selectedTable, true);
            rebuildTable(dialog, deselectedTable, false);
        }
    }

    private static <T extends UnlockableContent> void rebuildTable(BannedContentDialog dialog, Table table, boolean isSelected){
        table.clear();
        
        Log.info("upd");

        int buttonSize = settings.getInt("editor_content_buttons_size");
        ContentType type = Reflect.get(dialog, "type");
        Class<T> T = contentClasses.get(type);
        ObjectSet<T> contentSet = Reflect.get(dialog, "contentSet");
        Boolf<T> pred = Reflect.get(dialog, "pred");
        Seq<T> filteredContent = Reflect.get(dialog, "filteredContent");

        int cols;
        if(Core.graphics.isPortrait()){
            cols = Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 100f) / buttonSize));
        }else{
            cols = Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 300f) / buttonSize / 2));
        }

        if((isSelected && contentSet.isEmpty()) || (!isSelected && contentSet.size == content.<T>getBy(type).count(pred))){
            table.add("@empty").width(buttonSize * cols).padBottom(5f).get().setAlignment(Align.center);
            return;
        }

        Seq<T> array;
        if(!isSelected){
            array = content.getBy(type);
            array = array.copy();
            array.removeAll(contentSet.toSeq());
        }else{
            array = contentSet.toSeq();
        }
        array.sort();
        array.removeAll(content -> !filteredContent.contains(content));

        if(array.isEmpty()){
            table.add("@empty").width(buttonSize * cols).padBottom(5f).get().setAlignment(Align.center);
            return;
        }

        int i = 0;
        boolean requiresPad = true;

        for(var content : array){
            TextureRegion region = content.uiIcon;

            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNonei);
                button.getStyle().imageUp = new TextureRegionDrawable(region);
            button.resizeImage(buttonSize - 8f);
            if(isSelected) button.clicked(() -> {
                contentSet.remove(content);
                Reflect.invoke(dialog, "rebuildTables");
            });
            else button.clicked(() -> {
                contentSet.add(content);
                Reflect.invoke(dialog, "rebuildTables");
            });
            table.add(button).size(buttonSize).tooltip(content.localizedName);

            if(++i % cols == 0){
                table.row();
                requiresPad = false;
            }
        }

        if(requiresPad){
            table.add("").padRight(buttonSize * (cols - i));
        }
    }
}