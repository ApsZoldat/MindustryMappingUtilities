package mu.ui.data;

import arc.struct.*;
import arc.scene.ui.layout.*;
import mu.ui.*;
import mu.ui.dialogs.*;

import static mu.MUVars.*;

public class WindowData implements ElementData{
    public String name;

    // Position
    public float x = 0f, y = 0f;
    public boolean isDraggable = true;

    // Styling
    public float draggedAlpha = 0.45f;

    // Content
    public TableData cont;

    public WindowData(){
        Seq<String> existingNames = new Seq<>();

        for(WindowData data: windowsData){
            existingNames.add(data.name);
        }

        int i = 0;
        while(existingNames.contains("Window " + Integer.toString(++i))){};
        name = "Window " + Integer.toString(i);

        cont = new TableData();
        cont.marginTop = 50f;
        cont.marginLeft = 50f;
        cont.marginBot = 50f;
        cont.marginRight = 50f;

        cont.cells.add(new CellData(new ButtonData()));
        var c = new CellData(new ButtonData());
        c.endRow = true;
        c.padLeft = 20f;
        c.padBottom = 20f;
        cont.cells.add(c);
        cont.cells.add(new CellData(new ButtonData()));
        cont.cells.add(new CellData(new ButtonData()));
    }

    public Window build(){
        return new Window(this);
        // Yep it's that easy but only for WindowData
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();

        table.table(t -> {
            t.left();
            t.add("Name").left().padRight(5f);
            t.field(name, v -> {
                name = v;
                dialog.buildPath();
            }).width(300f);
        });

        return table;
    }
}