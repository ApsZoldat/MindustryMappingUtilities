package mu.ui.data;

import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.scene.style.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;
import mu.ui.dialogs.*;

public class TableData extends ElementData{
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
    // All fields that must be scaled through scl()
    public static Seq<String> sclFieldNames = new Seq<>(new String[]{"marginTop", "marginLeft", "marginBot", "marginRight"});

    public Table build(){
        Table table = new Table();

        copyFields(table);

        // Add all cells
        for(CellData cell : cells){
            cell.build(table);
        }

        table.invalidate();
        return table;
    }
    
    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.number(table, "MarginTop", f -> marginTop = f, () -> marginTop, 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginLeft", f -> marginLeft = f, () -> marginLeft, 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginBottom", f -> marginBot = f, () -> marginBot, 0f, Float.POSITIVE_INFINITY, 5f);
        dialog.number(table, "MarginRight", f -> marginRight = f, () -> marginRight, 0f, Float.POSITIVE_INFINITY, 5f);

        table.table(c -> {
            c.table(t -> {
                t.defaults().fillX().left();
                dialog.check(t, "Round", b -> round = b, () -> round);
                dialog.check(t, "Clip", b -> clip = b, () -> clip);
                t.button("Background", Icon.image, () -> {}).padTop(5f).size(200f, 50f).get().getLabel().setWrap(false);  // TODO
            }).growX().left();
            c.table(t -> {
                t.defaults().fillX().right();
                t.add("Alignment").padBottom(5f).row();
                t.table(a -> {
                    a.right();
                    dialog.alignment(a, v -> align = v);
                });  // TODO: this layout sucks
            }).growX().right();
        }).left().padTop(10f).fillX().row();

        table.button("Elements", Icon.wrench, () -> dialog.elementSelectionDialog()).padTop(10f).size(300f, 50f).center().row();
        table.button("Layout", Icon.menu, () -> dialog.layoutDialog.show(cells)).padTop(4f).size(300f, 50f).center();

        return table;
    }
}