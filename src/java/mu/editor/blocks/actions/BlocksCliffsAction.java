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

    public GridBits cliffGrid;
    public boolean down;

    public BlocksCliffsAction(boolean down){
        this.down = down;
    }

    public void startAction(){
        cliffGrid = new GridBits(editor.width(), editor.height());
        operation = new BlocksTilesOperation(editor.width(), editor.height());
    }

    public void startStep(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;
        cliffGrid.set(x, y);

        operation.setUpdated(tile);
    }

    public void endStep(){
        for(Tile tile : world.tiles){
            if(!cliffGrid.get(tile.x, tile.y)) continue;
    
            byte rotation = (down ? getCliffDownData(tile) : getCliffUpData(tile));

            if(rotation != 0){
                tile.setBlock(Blocks.cliff);
            }else{
                if(tile.block() == Blocks.cliff){
                    tile.setBlock(Blocks.air);
                }
            }
            tile.data = (byte) rotation;
        }
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
        return operation;
    }
}