package mu.ui.data;

import arc.struct.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mu.ui.*;
import mu.ui.dialogs.*;
import mu.utils.*;
import mu.utils.MUAnnotations.*;

import static mu.EditorVars.*;

public class WindowData extends UIObjectData{
    // Position
    public float x = 0f, y = 0f;
    public boolean isDraggable = true;

    // Styling
    public float draggedAlpha = 0.45f;

    // Content
    public TableData cont;

    public WindowData(){
        /*Seq<String> existingNames = new Seq<>();

        for(WindowData data: editorUi.windowsData){
            existingNames.add(data.name);
        }

        int i = 0;
        while(existingNames.contains("Window " + Integer.toString(++i))){};
        name = "Window " + Integer.toString(i);*/
        // TODO: fix ts
        name = "Window";

        cont = new TableData();
        cont.marginTop = 20f;
        cont.marginLeft = 20f;
        cont.marginBot = 20f;
        cont.marginRight = 20f;
    }

    public Window build(){
        Window window = new Window(this);
        cont.parent = this;
        this.object = window;
        runScript(buildScript);
        return window;
    }

    // This method shouldn't be called anywhere but just in case
    public Window buildPreview(UIExplorerDialog dialog){
        Window window = new Window(this);
        cont.parent = this;
        this.object = window;
        runScript(buildScript);
        window.isDraggable = false;
        return window;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        table.table(t -> {
            t.left();
            t.add("Name").left().padRight(5f);
            t.field(name, v -> {
                name = v;
                dialog.buildPath();
            }).width(300f);
        }).padBottom(10f);
        table.row();  // TODO: check name uniqueness

        dialog.number(table, "X", "x", Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 10f);
        dialog.number(table, "Y", "y", Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 10f);
        dialog.check(table, "IsDraggable", "isDraggable");
        dialog.number(table, "DraggedOpacity", "draggedAlpha", 0f, 1f, 0.05f);
        
        table.button("Table", Icon.menu, () -> {
            dialog.selectData(cont);
            dialog.build();
        }).center().padTop(20f).row();

        table.add(super.explorerSettings(dialog));
        return table;
    }
}