package mu.ui.dialogs;

import arc.graphics.*;
import arc.struct.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mu.ui.data.*;

import static mu.MUVars.*;

public class TableLayoutDialog extends BaseDialog{
    public Seq<CellData> cells;
    public Seq<CellData> selectedCells = new Seq<>();

    public Table selection = new Table();
    public boolean selectionMode = true;

    public TableLayoutDialog(){
        super("temp");

        addCloseButton();

        shown(this::build);
    }

    public void show(Seq<CellData> cells){
        this.cells = cells;
        show();
    }

    public void build(){
        cont.clear();

        buildSelection();
        cont.add(selection);
    }

    public void buildSelection(){
        var whiteui = (TextureRegionDrawable) Tex.whiteui;
        Drawable elemBack = whiteui.tint(0.7f, 0.3f, 0.3f, 1f);
        Drawable cellBack = whiteui.tint(0.3f, 0.5f, 0.3f, 0.5f);
        Drawable selectedElemBack = whiteui.tint(Color.valueOf("ffd37f"));
        Drawable selectedCellBack = whiteui.tint(Color.valueOf("ffd37f77"));
        
        selection.clear();

        for(CellData data : cells){
            Table outer = new Table();
            outer.setBackground((selectedCells.contains(data) ? selectedCellBack : cellBack));

            Table inner = new Table();
            inner.setBackground((selectedCells.contains(data) ? selectedElemBack : elemBack));

            Cell cell = outer.add(inner);

            // data.copyFields(cell);
            cell.pad(data.padTop, data.padLeft, data.padBottom, data.padRight);
            if(data.colspan > 1) cell.colspan(data.colspan);

            selection.add(outer);
            if(data.endRow) selection.row();
    
            outer.invalidate();
        }
        selection.invalidate();
    }
}
