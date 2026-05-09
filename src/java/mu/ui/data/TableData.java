package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

public class TableData{
    // All cells
    public Seq<CellData> cells;

    // Defaults
    public CellData cellDefaults;

    // Margins
    public float marginTop, marginLeft, marginBot, marginRight;

    // Styling
    public String backgroundName;
    public int align;
    public boolean round;
    public boolean clip;

    // All fields that can be set easily through Reflect
    public static Seq<String> fieldNames = new Seq<>(new String[]{"marginTop", "marginLeft", "marginBot", "marginRight", "align", "round", "clip"});

    public TableData(){
        cells = new Seq<>();
        cellDefaults = new CellData();
        
        marginTop = 0f;
        marginLeft = 0f;
        marginBot = 0f;
        marginRight = 0f;
        
        backgroundName = "";
        align = Align.center;
        round = true;
        clip = false;
    }
    
    public Table build(){
        Table table = new Table();

        for(String field : fieldNames){
            Reflect.set(table, field, Reflect.get(this, field));
        }

        // Add all cells
        for(CellData cell : cells){
            cell.build(table);
        }
        
        table.invalidate();
        return table;
    }
}