package mu.ui.data;

import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;
import mu.utils.*;
import mu.utils.MUAnnotations.*;

public class CellData extends UIObjectData{
    // Element
    public @NoCopy UIObjectData element = null;
    
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

    public CellData(){
        this.element = null;
    }

    public CellData(UIObjectData element, UIObjectData parent){
        this.parent = parent;
        if(element.isElementData()){
            this.element = element;
            element.parent = this;
        }else{
            this.element = null;
        }
    }

    @Override
    public boolean isElementData(){
        return false;
    }

    public Cell build(){
        if(parent == null || !(parent.object instanceof Table table)) return null;

        Cell cell;
        if(element == null || !element.isElementData()){
            cell = table.add("");
        }else{
            cell = table.add((Element)(element.build()));
        }

        if(endRow) table.row();

        MUReflect.copyFields(this, cell);

        runScript(buildScript);
        return cell;
    }

    public Cell buildPreview( UIExplorerDialog dialog){
        if(parent == null || !(parent.object instanceof Table table)) return null;

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

        if(element == null || !element.isElementData()){
            innerCell = elemTable.add("");
        }else{
            innerCell = elemTable.add((Element)(element.buildPreview(dialog)));
        }
        
        innerCell.pad(padTop, padLeft, padBottom, padRight);
        innerCell.align(align);
        innerCell.grow();
        
        Stack stack = new Stack(elemTable, button);
        Cell outerCell = table.add(stack);
        MUReflect.copyFields(this, outerCell);
        /*if(maxWidth != Float.POSITIVE_INFINITY) outerCell.maxWidth(maxWidth + padLeft + padRight);
        if(maxHeight != Float.POSITIVE_INFINITY) outerCell.maxHeight(maxHeight + padTop + padBottom);*/
        // TODO: fix ts
        
        outerCell.pad(0f);
        if(endRow) table.row();

        runScript(buildScript);
        return outerCell;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.number(table, "MinWidth", "minWidth", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MaxWidth", "maxWidth", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MinHeight", "minHeight", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MaxHeight", "maxHeight", 0f, Float.POSITIVE_INFINITY, 5f);
        table.table(t -> {
            t.button("No Max Width", () -> {
                dialog.currentGroup.each(c -> Reflect.set(c, "maxWidth", Float.POSITIVE_INFINITY));
            }).growX().uniformX();
            t.button("No Max Height", () -> {
                dialog.currentGroup.each(c -> Reflect.set(c, "maxHeight", Float.POSITIVE_INFINITY));
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

        table.add(super.explorerSettings(dialog));
        return table;
    }
}
