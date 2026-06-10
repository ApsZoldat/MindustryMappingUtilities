package mu.editor.blocks.operations;

import arc.struct.*;
import mindustry.world.*;

import static mu.EditorVars.*;

public class BlocksSelectionOperation implements BlocksOperation{
    public GridBits grid;
    public boolean select = true;
    public int startX = -1, startY = -1, endX = -1, endY = -1;

    public void start(){
        grid = new GridBits(editor.width(), editor.height());
    };

    public void stepStart(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;
        // Check if this tile actually changes before doing anything with it
        if(editor.blocksMode.selection.get(x, y) == select) return;

        grid.set(x, y);
        editor.blocksMode.selection.set(x, y, select);

        if(startX == -1){
            startX = endX = x;
            startY = endY = y;
        }
        startX = Math.min(x, startX);
        startY = Math.min(y, startY);
        endX = Math.max(x, endX);
        endY = Math.max(y, endY);
    }

    public void stepEnd(){
        return;
    }

    public void end(){
        if(startX == -1) return;  // Should never happen but still

        // Crop grid to its final size
        GridBits newGrid = new GridBits(endX - startX + 1, endY - startY + 1);
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                newGrid.set(x, y, grid.get(x + startX, y + startY));
            }
        }
        grid = newGrid;
    }

    public void undo(){
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                if(!grid.get(x, y)) continue;

                editor.blocksMode.selection.set(x + startX, y + startY, !select);
            }
        }
    }

    public void redo(){
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                if(!grid.get(x, y)) continue;

                editor.blocksMode.selection.set(x + startX, y + startY, select);
            }
        }
    }
}