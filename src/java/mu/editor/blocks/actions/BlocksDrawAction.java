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

    public void enableDataType(TileData type){
        this.dataTypes |= (1 << type.ordinal());
    }

    public void disableDataType(TileData type){
        this.dataTypes &= ~(1 << type.ordinal());
    }

    public void toggleDataType(TileData type){
        this.dataTypes ^= (1 << type.ordinal());
    }

    public void startAction(){
        operation = new BlocksTilesOperation(editor.width(), editor.height());
    }

    public void startStep(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;

        for(TileData type : TileData.values()){
            if((dataTypes & (1 << type.ordinal())) == 0) continue;

            Object oldData = BlocksTilesOperation.getTileData(type, tile);
            Object newData = null;
            switch(type){
                case floor -> newData = this.floor;
                case overlay -> newData = this.overlay;
                case block -> newData = this.block;
                case team -> newData = this.team;
                case rotation -> newData = this.rotation;
                case data -> newData = this.data;
                case extraData -> newData = this.extraData;
            }
            if(oldData == newData) continue;  // nothing ever changes
            BlocksTilesOperation.setTileData(type, tile, newData);
            operation.addTileChange(type, tile, oldData, newData);
        }
        operation.setUpdated(tile);
    }

    public void endStep(){
        operation.updateRenderer();
    }

    public EditorOperation endAction(){
        return operation;
    }
}