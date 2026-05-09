package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.scene.ui.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

public class CellData{
    // Element
    public Element element;
    
    // Sizing
    public float minWidth, minHeight, maxWidth, maxHeight;
    
    // Padding
    public float padTop, padLeft, padBottom, padRight;
    
    // Filling & expansion
    public float fillX, fillY;
    public int expandX, expandY;
    
    // Alignment
    public int align;
    
    // Layout
    public boolean uniformX, uniformY, endRow;
    public int colspan;

    // All fields that can be set easily through Reflect
    public static Seq<String> fieldNames = new Seq<>(new String[]{"minWidth", "maxWidth", "minHeight", "maxHeight", "padTop", "padLeft", "padBottom", "padRight", "fillX", "fillY", "expandX", "expandY", "align", "uniformX", "uniformY", "endRow", "colspan"});

    public CellData(){
        element = new ImageButton(Icon.wrench);
        minWidth = Float.NEGATIVE_INFINITY;
        minHeight = Float.NEGATIVE_INFINITY;
        maxWidth = Float.NEGATIVE_INFINITY;
        maxHeight = Float.NEGATIVE_INFINITY;
        padTop = 0f;
        padLeft = 0f;
        padBottom = 0f;
        padRight = 0f;
        fillX = 0f;
        fillY = 0f;
        expandX = 0;
        expandY = 0;
        align = Align.center;
        uniformX = false;
        uniformY = false;
        endRow = false;
        colspan = 1;
    }

    public Cell build(Table table){
        Cell cell = table.add(element);
        if(endRow) table.row();

        for(String field : fieldNames){
            Reflect.set(cell, field, Reflect.get(this, field));
        }

        cell.size(50f);

        return cell;
    }
}
