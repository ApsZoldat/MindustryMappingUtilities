package mu.ui.data;

import arc.struct.*;
import mu.ui.*;

import static mu.MUVars.*;

public class WindowData{
    public String name;

    // Position
    public float x, y;

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

        x = 0f;
        y = 0f;

        cont = new TableData();
        cont.marginTop = 50f;
        cont.marginLeft = 50f;
        cont.marginBot = 50f;
        cont.marginRight = 50f;

        cont.cells.add(new CellData());
        var c = new CellData();
        c.endRow = true;
        c.padLeft = 20f;
        c.padBottom = 20f;
        cont.cells.add(c);
        cont.cells.add(new CellData());
        cont.cells.add(new CellData());
    }

    public Window build(){
        return new Window(this);
        // Yep it's that easy but only for WindowData
    }
}