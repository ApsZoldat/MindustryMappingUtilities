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
    public boolean uniformX = false, uniformY = false;
    public @NoCopy boolean endRow = false;  // it is handled by table.row()
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
                button.setColor(0.4f, 0.8f, 0.4f, 0.6f);
            }else{
                button.setColor(0.6f, 0.3f, 0.3f, 0.6f);
            }
            button.setChecked(true);
        });  // TODO: proper style
        button.clicked(() -> {
            if(dialog.selectedCells.contains(this)){
                dialog.selectedCells.remove(this);
            }else{
                dialog.selectedCells.add(this);
            }
        });

        Table elemTable = new Table();
        Cell innerCell;

        if(element == null){
            innerCell = elemTable.add("");
        }else{
            innerCell = elemTable.add(element.buildPreview(dialog));
        }
        
        innerCell.pad(padTop, padLeft, padBottom, padRight);
        innerCell.align(align);
        innerCell.grow();
        
        Stack stack = new Stack(elemTable, button);
        Cell outerCell = table.add(stack);
        copyFields(outerCell);
        /*if(maxWidth != Float.POSITIVE_INFINITY) outerCell.maxWidth(maxWidth + padLeft + padRight);
        if(maxHeight != Float.POSITIVE_INFINITY) outerCell.maxHeight(maxHeight + padTop + padBottom);*/
        // TODO: fix ts
        
        outerCell.pad(0f);
        if(endRow) table.row();

        return outerCell;
    }

    public static Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.number(table, "MinWidth", "minWidth", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MaxWidth", "maxWidth", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MinHeight", "minHeight", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MaxHeight", "maxHeight", 0f, Float.POSITIVE_INFINITY, 5f);
        table.table(t -> {
            t.button("No Max Width", () -> {
                dialog.getCurrentGroup().each(c -> Reflect.set(c, "maxWidth", Float.POSITIVE_INFINITY));
            }).growX().uniformX();
            t.button("No Max Height", () -> {
                dialog.getCurrentGroup().each(c -> Reflect.set(c, "maxHeight", Float.POSITIVE_INFINITY));
            }).growX().uniformX();
        }).padBottom(5f).padTop(3f).fillX().row();
        // TODO: move pads and other stuff from explorer methods here

        dialog.number(table, "PadTop", "padTop", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadLeft", "padLeft", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadBottom", "padBottom", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "PadRight", "padRight", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.numberi(table, "Colspan", "colspan", 1, Integer.MAX_VALUE, 1);
        dialog.check(table, "EndRow", "endRow");

        dialog.number(table, "FillX", "fillX", 0f, Float.POSITIVE_INFINITY, 1f);
        dialog.number(table, "FillY", "fillY", 0f, Float.POSITIVE_INFINITY, 1f);
        dialog.numberi(table, "ExpandX", "expandX", 0, Integer.MAX_VALUE, 1);
        dialog.numberi(table, "ExpandY", "expandY", 0, Integer.MAX_VALUE, 1);

        table.table(c -> {
            c.table(t -> {
                t.defaults().fillX().left();
                dialog.check(t, "UniformX", "uniformX");
                dialog.check(t, "UniformY", "uniformY");
            }).growX().left();
            c.table(t -> {
                t.defaults().fillX().right();
                t.add("Alignment").padBottom(5f).row();
                t.table(a -> {
                    a.right();
                    dialog.alignment(a, "align");
                });  // TODO: this layout sucks
            }).growX().right();
        }).left().padTop(10f).fillX().row();

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
