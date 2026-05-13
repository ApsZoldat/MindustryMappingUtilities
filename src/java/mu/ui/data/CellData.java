package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.scene.ui.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;
import mu.ui.data.annotations.*;

public class CellData{
    // Element
    public @NoCopy ElementData element;
    
    // Sizing
    public @RequireScl float minWidth = Float.NEGATIVE_INFINITY, minHeight = Float.NEGATIVE_INFINITY, maxWidth = Float.NEGATIVE_INFINITY, maxHeight = Float.NEGATIVE_INFINITY;
    
    // Padding
    public @RequireScl float padTop = 0f, padLeft = 0f, padBottom = 0f, padRight = 0f;
    
    // Filling & expansion
    public float fillX = 0f, fillY = 0f;
    public int expandX = 0, expandY = 0;
    
    // Alignment
    public int align = Align.center;
    
    // Layout
    public boolean uniformX = false, uniformY = false, endRow = false;
    public int colspan = 1;

    public CellData(ElementData element){
        this.element = element;
    }

    public Cell build(Table table){
        Cell cell;
        if(element == null){
            cell = table.add("");
        }else{
            cell = table.add(element.build());
        }
        if(endRow) table.row();

        copyFields(cell);

        return cell;
    }

    public Cell buildPreview(Table table, UIExplorerDialog dialog){
        Button button = new Button(Styles.underlineb);
        button.update(() -> {
            if(dialog.selectedCells.contains(this)){
                button.setColor(0.3f, 0.7f, 0.3f, 0.6f);
            }else{
                button.setColor(0.6f, 0.3f, 0.3f, 0.6f);
            }
        });
        button.clicked(() -> {
            if(dialog.selectedCells.contains(this)){
                dialog.selectedCells.remove(this);
            }else{
                dialog.selectedCells.add(this);
            }
        });

        Table elemTable = new Table();
        Cell cell;

        if(element == null){
            cell = elemTable.add("");
        }else{
            cell = elemTable.add(element.buildPreview(dialog));
        }
        Stack stack = new Stack(elemTable, button);
        table.add(stack);
        if(endRow) table.row();

        copyFields(cell);
        elemTable.invalidate();

        return cell;
    }

    public static Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();

        dialog.number(table, "PadTop", "padTop", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadLeft", "padLeft", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadBottom", "padBottom", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadRight", "padRight", 0f, Float.POSITIVE_INFINITY, 5f);

        return table;
    }

    // TODO: move to utils
    public void copyFields(Cell cell){
        for(var field : this.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(NoCopy.class)) continue;

            String name = field.getName();
            if(field.isAnnotationPresent(RequireScl.class)){
                Reflect.set(cell, name, Scl.scl(Reflect.get(this, name)));
            }else{
                Reflect.set(cell, name, Reflect.get(this, name));
            }
        }
    }
}
