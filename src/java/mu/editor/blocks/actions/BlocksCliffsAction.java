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

        byte rotation = 0;
        for(int i = 0; i < 8; ++i){
            Tile other = world.tiles.get(tile.x + Geometry.d8[i].x, tile.y + Geometry.d8[i].y);
            if(other != null){
                boolean otherBit = cliffGrid.get(other.x, other.y);
                if (!otherBit) {
                    rotation |= (1 << i);
                }
            }
        }

        // TODO: i am SICK of it, write some clever bitwise operation for down mode here later.

        if(rotation != 0){
            tile.setBlock(Blocks.cliff);
            tile.data = (byte) rotation;
        }else{
            if(tile.block() == Blocks.cliff){
                tile.setBlock(Blocks.air);
                tile.data = 0;
            }
        }
        operation.updateRenderer();
    }

    public EditorOperation endAction(){
        return operation;
    }
}