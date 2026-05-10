package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mu.ui.dialogs.*;

public class TableData implements ElementData{
    // All cells
    public Seq<CellData> cells = new Seq<>();

    // Defaults
    public CellData cellDefaults = new CellData(null);

    // Margins
    public float marginTop = 0f, marginLeft = 0f, marginBot = 0f, marginRight = 0f;

    // Styling
    public String backgroundName = "";
    public int align = Align.center;
    public boolean round = true;
    public boolean clip = false;

    // All fields that can be set easily through Reflect
    public static Seq<String> fieldNames = new Seq<>(new String[]{"marginTop", "marginLeft", "marginBot", "marginRight", "align", "round", "clip"});
    
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
    
    public Table explorerSettings(UIExplorerDialog dialog){
        return new Table();
    }
}