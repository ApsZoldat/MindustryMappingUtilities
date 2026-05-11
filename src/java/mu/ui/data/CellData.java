package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.scene.ui.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

public class CellData{
    // Element
    public ElementData element;
    
    // Sizing
    public float minWidth = Float.NEGATIVE_INFINITY, minHeight = Float.NEGATIVE_INFINITY, maxWidth = Float.NEGATIVE_INFINITY, maxHeight = Float.NEGATIVE_INFINITY;
    
    // Padding
    public float padTop = 0f, padLeft = 0f, padBottom = 0f, padRight = 0f;
    
    // Filling & expansion
    public float fillX = 0f, fillY = 0f;
    public int expandX = 0, expandY = 0;
    
    // Alignment
    public int align = Align.center;
    
    // Layout
    public boolean uniformX = false, uniformY = false, endRow = false;
    public int colspan = 1;

    // All fields that can be set easily through Reflect
    public static Seq<String> fieldNames = new Seq<>(new String[]{"minWidth", "maxWidth", "minHeight", "maxHeight", "padTop", "padLeft", "padBottom", "padRight", "fillX", "fillY", "expandX", "expandY", "align", "uniformX", "uniformY", "endRow", "colspan"});
    // All fields that must be scaled through scl()
    public static Seq<String> sclFieldNames = new Seq<>(new String[]{"minWidth", "maxWidth", "minHeight", "maxHeight", "padTop", "padLeft", "padBottom", "padRight"});

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

    public void copyFields(Cell cell){
        for(String field : fieldNames){
            if(sclFieldNames.contains(field)){
                Reflect.set(cell, field, Scl.scl(Reflect.get(this, field)));
            }else{
                Reflect.set(cell, field, Reflect.get(this, field));
            }
        }
    }
}
