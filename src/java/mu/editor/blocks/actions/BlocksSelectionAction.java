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
        operation = new BlocksSelectionOperation(editor.width(), editor.height(), select);
    }

    public void startStep(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;
        // Check if this tile actually changes before doing anything with it
        if(editor.blocksMode.selection.get(x, y) == select) return;
        editor.blocksMode.selection.set(x, y, select);
        operation.addTile(x, y);
    }

    public void endStep(){
        return;
    }

    public EditorOperation endAction(){
        operation.cropGrid();
        return operation;
    }
}