package mu.editor.blocks.operations;

import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import mu.editor.*;
import mu.utils.*;

import static mu.EditorVars.*;

public class BlocksSelectionOperation implements EditorOperation{
    public ChunkedGridBits updatedTiles;
    public boolean select = true;

    public BlocksSelectionOperation(boolean select){
        updatedTiles = new ChunkedGridBits();
        this.select = select;
    }

    public void addTile(int x, int y){
        updatedTiles.set(x, y);
    }

    public void undo(){
        updatedTiles.each((x, y) -> {
            editor.blocksMode.selection.set(x, y, !select);
        });
    }

    public void redo(){
        updatedTiles.each((x, y) -> {
            editor.blocksMode.selection.set(x, y, select);
        });
    }
}