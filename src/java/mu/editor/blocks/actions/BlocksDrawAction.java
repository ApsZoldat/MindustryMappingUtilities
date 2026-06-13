package mu.editor.blocks.actions;

import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.*;
import mu.editor.blocks.operations.*;

import static mu.EditorVars.*;

public class BlocksDrawAction implements BlocksAction{
    public BlocksTilesOperation operation;

    public byte dataTypes = 0;  // Bitset for telling which of the following to draw
    public Floor floor = null;
    public Floor overlay = null;
    public Block block = null;
    public Team team = null;
    public int rotation = 0;
    public int data = 0;
    public int extraData = 0;

    public void copyData(TileData type, Tile tile){
        this.dataTypes |= (1 << type.ordinal());

        switch(type){
            case floor -> this.floor = tile.floor();
            case overlay -> this.overlay = tile.overlay();
            case block -> this.block = tile.block();
            case team -> this.team = tile.team();
            case rotation -> this.rotation = (tile.build == null ? 0 : tile.build.rotation);
            case data -> {
                this.data = TileData.packMergedData(tile.data, tile.overlayData, tile.floorData);
            }
            case extraData -> this.extraData = tile.extraData;
        }
    }

    public void removeData(TileData type){
        this.dataTypes &= ~(1 << type.ordinal());
    }

    public void startAction(){
        operation = new BlocksTilesOperation(editor.width(), editor.height());
    }

    public void startStep(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;

        //if(editor.blocksMode.selection.get(x, y) == select) return;
        //operation.addTile(x, y);
    }

    public void endStep(){
        return;
    }

    public EditorOperation endAction(){
        return operation;
    }
}