package mu.editor.blocks.actions;

import mindustry.world.*;
import mu.editor.*;
import mu.editor.blocks.operations.*;

import static mu.EditorVars.*;

public class BlocksSelectionAction implements BlocksAction{
    public boolean select;
    public BlocksSelectionOperation operation;

    public BlocksSelectionAction(boolean select){
        this.select = select;
    }

    public void startAction(){
        operation = new BlocksSelectionOperation(select);
    }

    public void startStep(){
        return;
    }

    public void act(Tile tile){
        // Check if this tile actually changes before doing anything with it
        if(editor.blocksMode.selection.get(tile.x, tile.y) == select) return;
        editor.blocksMode.selection.set(tile.x, tile.y, select);
        operation.addTile(tile.x, tile.y);
    }

    public void endStep(){
        return;
    }

    public EditorOperation endAction(){
        return operation;
    }
}