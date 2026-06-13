package mu.editor.blocks.operations;

import arc.struct.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.*;
import mu.utils.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksTilesOperation implements EditorOperation{
    public static Seq<Class<?>> dataTypeClasses = Seq.with(Floor.class, Floor.class, Block.class, Team.class, Integer.class, Integer.class, Integer.class);

    public GridBits updatedTiles;
    public GridBits blockUpdates;  // Tiles which require block update in the renderer
    public GridBits staticUpdates;  // Tiles which require static update in the renderer

    public TilesState oldState;
    public TilesState newState;

    public BlocksTilesOperation(int width, int height){
        updatedTiles = new GridBits(width, height);
        blockUpdates = new GridBits(width, height);
        staticUpdates = new GridBits(width, height);

        oldState = new TilesState();
        newState = new TilesState();
    }

    public void act(Tile tile){
        int x = (int)tile.x, y = (int)tile.y;

        // Check that this tile was not changed before
        /*if(grid.get(x, y)) return;
        grid.set(x, y);

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

    public void undo(){
        return;
    }

    public void redo(){
        return;
    }

    public void updateRenderer(){
        for(int x = 0; x < blockUpdates.width(); x++){
            for(int y = 0; y < blockUpdates.height(); y++){
                if(blockUpdates.get(x, y)) editor.updateRendererBlock(x, y);
                if(staticUpdates.get(x, y)) editor.updateRendererStatic(x, y);
            }
        }
        blockUpdates.clear();
        staticUpdates.clear();
    }

    public class TilesState{
        public ObjectMap<Floor, ChunkedGridBits> floors = new ObjectMap<>();
        public ObjectMap<Floor, ChunkedGridBits> overlays = new ObjectMap<>();
        public ObjectMap<Block, ChunkedGridBits> blocks = new ObjectMap<>();
        public ObjectMap<Team, ChunkedGridBits> teams = new ObjectMap<>();
        public ObjectMap<Integer, ChunkedGridBits> rotations = new ObjectMap<>();
        public ObjectMap<Integer, ChunkedGridBits> datas = new ObjectMap<>();
        public ObjectMap<Integer, ChunkedGridBits> extraDatas = new ObjectMap<>();

        public Seq<ObjectMap<?, ChunkedGridBits>> allMaps = new Seq<>();

        public TilesState(){
            allMaps.add(floors);
            allMaps.add(overlays);
            allMaps.add(blocks);
            allMaps.add(teams);
            allMaps.add(rotations);
            allMaps.add(datas);
            allMaps.add(extraDatas);
        }

        // TODO: maybe simple Ctrl+C Ctrl+V switch is still better...
        public void addData(TileData type, Object value, int x, int y){
            int index = type.ordinal();
            Class<?> expectedType = dataTypeClasses.get(index);
            if(!expectedType.isInstance(value)) return;

            Object casted = expectedType.cast(value);

            ObjectMap<Object, ChunkedGridBits> map = (ObjectMap<Object, ChunkedGridBits>) allMaps.get(index);

            if(!map.containsKey(casted)){
                map.put(casted, new ChunkedGridBits());
            }
            map.get(casted).set(x, y);
        }

        /*public void addData(TileData type, Object value, int x, int y){
            int index = type.ordinal();
            if(allTypes.get(index).isInstance(value)){
                Object casted = allTypes.get(index).cast(value);
                if(!allMaps.get(index).containsKey(casted)){
                    allMaps.get(index).put(casted, new ChunkedGridBits());
                }
                allMaps.get(index).get(casted).set(x, y);
            }
        }*/

        public void removeData(TileData type, int x, int y){
            int index = type.ordinal();
            for(var grid : allMaps.get(index).values()){
                grid.set(x, y, false);
            }
        }
    }
}