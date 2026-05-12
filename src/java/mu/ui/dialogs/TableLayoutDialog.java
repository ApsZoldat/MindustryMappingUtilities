package mu.ui.dialogs;

import arc.graphics.*;
import arc.struct.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mu.ui.dialogs.UIExplorerDialog.*;
import mu.ui.data.*;

import static mu.MUVars.*;

public class TableLayoutDialog extends BaseDialog{
    public Seq<CellData> cells;
    public Seq<CellData> selectedCells = new Seq<>();

    public Table selectionTable = new Table();
    public Table editTable = new Table();
    public boolean editMode = true;

    public TableLayoutDialog(){
        super("temp");

        addCloseButton();

        buttons.button("temp", Icon.edit, () -> {
            editMode = !editMode;
            build();
        }).get().setDisabled(() -> (!editMode && selectedCells.isEmpty()));

        shown(this::build);
    }

    public void show(Seq<CellData> cells){
        this.cells = cells;
        show();
    }

    public void build(){
        cont.clear();

        if(editMode){
            buildSelectionTable();
            cont.add(selectionTable);
        }else{
            buildEditTable();
            cont.add(editTable);
        }
    }

    public void buildSelectionTable(){
        var whiteui = (TextureRegionDrawable) Tex.whiteui;
        Drawable elemBack = whiteui.tint(0.5f, 0.5f, 0.5f, 1f);
        Drawable cellBack = whiteui.tint(0.3f, 0.3f, 0.3f, 0.5f);
        Drawable selectedElemBack = whiteui.tint(Color.valueOf("ffd37f"));
        Drawable selectedCellBack = whiteui.tint(Color.valueOf("ffd37f77"));
        
        selectionTable.clear();

        for(CellData data : cells){
            Table cellArea = new Table();
            cellArea.setBackground((selectedCells.contains(data) ? selectedCellBack : cellBack));
            cellArea.touchable = Touchable.enabled;
            cellArea.clicked(() -> {
                if(selectedCells.contains(data)){
                    selectedCells.remove(data);
                }else{
                    selectedCells.add(data);
                }
                buildSelectionTable();
            });

            Table elemArea = new Table();
            elemArea.setBackground((selectedCells.contains(data) ? selectedElemBack : elemBack));

            Cell cell = cellArea.add(elemArea);

            data.copyFields(cell);
            cell.minWidth(Math.max(data.minWidth, 0f));
            cell.minHeight(Math.max(data.minHeight, 0f));
            // TODO: Later try creating more broken cells and fixing more bugs

            selectionTable.add(cellArea);
            if(data.endRow) selectionTable.row();
    
            cellArea.invalidate();
        }
        selectionTable.invalidate();
    }

    public void buildEditTable(){
        editTable.clear();

        // TODO: add <empty>
        if(selectedCells.size == 0) return;

        UIExplorerDialog.number(editTable, "PadTop", f -> selectedCells.each(c -> c.padTop = f), () -> selectedCells.get(0).padTop, 0f, Float.POSITIVE_INFINITY, 5f);
        UIExplorerDialog.number(editTable, "PadLeft", f -> selectedCells.each(c -> c.padLeft = f), () -> selectedCells.get(0).padLeft, 0f, Float.POSITIVE_INFINITY, 5f);
        UIExplorerDialog.number(editTable, "PadBottom", f -> selectedCells.each(c -> c.padBottom = f), () -> selectedCells.get(0).padBottom, 0f, Float.POSITIVE_INFINITY, 5f);
        UIExplorerDialog.number(editTable, "PadRight", f -> selectedCells.each(c -> c.padRight = f), () -> selectedCells.get(0).padRight, 0f, Float.POSITIVE_INFINITY, 5f);
    }
}
