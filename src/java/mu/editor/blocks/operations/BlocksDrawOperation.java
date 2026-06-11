package mu.editor.blocks.operations;

import arc.struct.*;
import mindustry.world.*;
import mu.editor.*;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksDrawOperation implements EditorOperation{
    public GridBits grid;  // already changed tiles

    public void start(){
        grid = new GridBits(editor.width(), editor.height());
    };

    public void stepStart(){
        return;
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;

        // Check that this tile was not changed before
        /*if(grid.get(x, y)) return;
        grid.set(x, y);

        boolean updateBlock = false;
        boolean updateStatic = false;

        if(block != null && !block.isMultiblock() && !tile.block().isMultiblock()){
            tile.setBlock(block, (team == null ? Team.sharded : team), (rotation == -1 ? 0 : rotation));
            updateBlock = true;
        }else if(block == null){  // Just change team/rotation
            if(team != null || tile.build != null){
                tile.build.team(team);
                updateBlock = true;
            }
            if(rotation != -1 || tile.build != null){
                tile.build.rotation = (byte)rotation;
                updateBlock = true;
            }
        }

        if(floor != null){
            
            tile.setFloor(floor);
            updateStatic = true;
        }

        if(overlay != null){
            tile.setOverlay(overlay);
            updateStatic = true;
        }

        if(updateBlock) editor.updateRendererBlock(tile.x, tile.y);
        if(updateStatic) editor.updateRendererStatic(tile.x, tile.y);*/
    }

    public void stepEnd(){
        return;
    }

    public void end(){
        grid = null;  // no need to store this now
    }

    public void undo(){
        return;
    }

    public void redo(){
        return;
    }

    /** Class storing all the tiles changes of a certain type (block, floor, data, etc.) */
    /*public abstract class TileOperations{
        public ShortSeq x = new ShortSeq();
        public ShortSeq y = new ShortSeq();
        public IntSeq data = new IntSeq();

        public abstract void addOperation(int x, int y, Tile tile);

        public abstract void applyOperation(Tile tile, int data);

        public void addOperation(int x, int y, int data){
            this.x.add((short)x);
            this.y.add((short)y);
            this.data.add(data);
        }

        public void applyOperations(){
            for(int i = 0; i < data.size; i++){
                Tile tile = world.tiles.get((int)(x.get(i)), (int)(y.get(i)));
                if(tile == null) continue;
                applyOperation(tile, data.get(i));
            }
        }
    }

    public class TileBlockOperations extends TileOperations{
        public abstract void addOperation(int x, int y, Tile tile){
            addOperation(x, y, tile.blockID());
        }

        public abstract void applyOperation(Tile tile, int data){
            Block block = content.block(to);
            tile.setBlock(block, tile.team(), tile.build == null ? 0 : tile.build.rotation);
            if(tile.build != null){
                tile.build.enabled = true;
            }
        }
    }

    public class TileFloorOperations extends TileOperations{
        public abstract void addOperation(int x, int y, Tile tile){
            addOperation(x, y, tile.floorID());
        }

        public abstract void applyOperation(Tile tile, int data){
            if(content.block(to) instanceof Floor floor){
                tile.setFloor(floor);
                editor.updateRendererStatic(tile.x, tile.y);
            }
        }
    }

    public class TileOverlayOperations extends TileOperations{
        public abstract void addOperation(int x, int y, Tile tile){
            addOperation(x, y, tile.overlayID());
        }

        public abstract void applyOperation(Tile tile, int data){
            if(content.block(to) instanceof Floor floor){
                tile.setOverlay(floor);
                editor.updateRendererStatic(tile.x, tile.y);
            }
        }
    }

    public class TileRotationsOperations extends TileOperations{
        public abstract void addOperation(int x, int y, Tile tile){
            addOperation(x, y, (tile.build == null ? 0 : (byte)tile.build.rotation));
        }

        public abstract void applyOperation(Tile tile, int data){
            if(content.block(to) instanceof Floor floor){
                if(tile.build != null){
                    tile.build.rotation = to;
                tile.setOverlay(floor);
                editor.updateRendererStatic(tile.x, tile.y);
            }
        }
    }

    public enum TileOperationType{
        block,
        floor,
        overlay,
        rotation,
        team,
        data,
        extraData,
        building
    }*/
}