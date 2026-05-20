package mu.ui.data;

import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;
import mu.ui.dialogs.*;
import mu.utils.*;
import mu.utils.MUAnnotations.*;

public class TableData implements UIObjectData, ElementData{
    // All cells
    public @NoCopy Seq<CellData> cells = new Seq<>();

    // Defaults
    // public @NoCopy CellData cellDefaults = new CellData(null);

    // Margins
    public @RequireScl float marginTop = 0f, marginLeft = 0f, marginBot = 0f, marginRight = 0f;

    // Styling
    public @NoCopy String backgroundName = "";
    public int align = Align.center;
    public boolean round = true;
    public boolean clip = false;

    public Table build(){
        Table table = new Table();

        MUReflect.copyFields(this, table);

        // Add all cells
        for(CellData cell : cells){
            cell.build(table);
        }

        table.invalidate();
        return table;
    }

    public Table buildPreview(UIExplorerDialog dialog){
        Table table = new Table();

        MUReflect.copyFields(this, table);

        // Add all cells
        for(CellData cell : cells){
            cell.buildPreview(table, dialog);
        }

        table.invalidate();
        return table;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.number(table, "MarginTop", "marginTop", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginLeft", "marginLeft", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginBottom", "marginBot", 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginRight", "marginRight", 0f, Float.POSITIVE_INFINITY, 5f);

        table.table(c -> {
            c.table(t -> {
                t.defaults().fillX().left();
                dialog.check(t, "Round", "round");
                dialog.check(t, "Clip", "clip");
                t.button("Background", Icon.image, () -> {}).padTop(5f).size(200f, 50f).get().getLabel().setWrap(false);  // TODO
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

        table.button("Layout", Icon.menu, () -> dialog.layoutDialog()).padTop(10f).size(300f, 50f).center();

        return table;
    }
}