package mu.editor.blocks.actions;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.*;
import mu.editor.blocks.operations.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksCliffsAction implements BlocksAction{
    public BlocksTilesOperation operation;

    public ChunkedGridBits cliffGrid;
    public boolean down;

    public BlocksCliffsAction(boolean down){
        this.down = down;
    }

    public void startAction(){
        cliffGrid = new ChunkedGridBits();
        operation = new BlocksTilesOperation();
    }

    public void startStep(Tile tile){
        return;
    }

    public void act(Tile tile){
        cliffGrid.set(tile.x, tile.y);

        if(!operation.updatedTiles.get(tile.x, tile.y)){
            operation.oldState.addData(TileData.block, tile, BlocksTilesOperation.getTileData(TileData.block, tile));
            operation.oldState.addData(TileData.data, tile, BlocksTilesOperation.getTileData(TileData.data, tile));
            tile.getLinkedTiles(t -> operation.setUpdated(t));
        }
    }

    public void endStep(Tile tile){
        cliffGrid.each((x, y) -> {
            Tile t = world.tiles.get(x, y);
            byte rotation = (down ? getCliffDownData(t) : getCliffUpData(t));

            if(rotation != 0){
                if(t.build != null){
                    operation.oldState.addBuilding(t.build);
                }
                t.setBlock(Blocks.cliff);
            }else{
                operation.oldState.loadTile(t);
            }
            t.data = (byte)rotation;
        });
        operation.updateRenderer();
    }

    // TODO: custom cliff data strategies
    public byte getCliffUpData(Tile tile){
        byte rotation = 0;
        for(int i = 0; i < 8; ++i){
            Tile other = world.tiles.get(tile.x + Geometry.d8[i].x, tile.y + Geometry.d8[i].y);
            if(other == null) continue;
    
            if(!cliffGrid.get(other.x, other.y)) rotation |= (1 << i);
        }
        return rotation;
    }

    public byte getCliffDownData(Tile tile){
        if(isMiddleTile(tile)) return 0;

        byte rotation = 0;
        for(int i = 0; i < 8; ++i){
            Tile other = world.tiles.get(tile.x + Geometry.d8[i].x, tile.y + Geometry.d8[i].y);
            if(other == null) continue;

            if(cliffGrid.get(other.x, other.y) && isMiddleTile(other)) rotation |= (1 << i);
        }
        return rotation;
    }

    public boolean isMiddleTile(Tile tile){
        for(int i = 0; i < 4; ++i){
            if(!cliffGrid.get((tile.x + Geometry.d4[i].x), (tile.y + Geometry.d4[i].y))){
                return false;
            }
        }
        return true;
    }

    public EditorOperation endAction(){
        cliffGrid.each((x, y) -> {
            Tile tile = world.tiles.get(x, y);
            if(tile == null) return;  // how though?
            if(tile.block() != Blocks.cliff) return;
            operation.newState.addData(TileData.block, tile, Blocks.cliff);
            operation.newState.addData(TileData.data, tile, BlocksTilesOperation.getTileData(TileData.data, tile));
        });
        return operation;
    }
}