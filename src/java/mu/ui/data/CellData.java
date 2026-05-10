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

    public CellData(ElementData element){
        this.element = element;
    }

    public Cell build(Table table){
        Cell cell = table.add(element.build());
        if(endRow) table.row();

        for(String field : fieldNames){
            Reflect.set(cell, field, Reflect.get(this, field));
        }

        cell.size(50f);

        return cell;
    }
}
