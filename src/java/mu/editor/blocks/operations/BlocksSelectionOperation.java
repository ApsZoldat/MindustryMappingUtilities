package mu.editor.blocks.operations;

import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import mu.editor.*;

import static mu.EditorVars.*;

public class BlocksSelectionOperation implements EditorOperation{
    public GridBits updatedTiles;
    public boolean select = true;
    public int startX = -1, startY = -1, endX = -1, endY = -1;

    public BlocksSelectionOperation(int width, int height, boolean select){
        updatedTiles = new GridBits(width, height);
        this.select = select;
    }

    public void addTile(int x, int y){
        updatedTiles.set(x, y);

        if(startX == -1){
            startX = endX = x;
            startY = endY = y;
        }
        startX = Math.min(x, startX);
        startY = Math.min(y, startY);
        endX = Math.max(x, endX);
        endY = Math.max(y, endY);
    }

    public void cropGrid(){
        if(startX == -1) return;  // Should never happen but still

        // Crop grid to its final size
        GridBits newGrid = new GridBits(endX - startX + 1, endY - startY + 1);
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                newGrid.set(x, y, updatedTiles.get(x + startX, y + startY));
            }
        }
        updatedTiles = newGrid;
    }

    public void undo(){
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                if(!updatedTiles.get(x, y)) continue;

                editor.blocksMode.selection.set(x + startX, y + startY, !select);
            }
        }
    }

    public void redo(){
        for(int x = 0; x < (endX - startX + 1); x++){
            for(int y = 0; y < (endY - startY + 1); y++){
                if(!updatedTiles.get(x, y)) continue;

                editor.blocksMode.selection.set(x + startX, y + startY, select);
            }
        }
    }
}