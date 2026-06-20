package mu.editor.blocks.operations;

import arc.func.*;
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

    public ChunkedGridBits updatedTiles;

    public TilesState oldState;
    public TilesState newState;

    public BlocksTilesOperation(){
        updatedTiles = new ChunkedGridBits();

        oldState = new TilesState();
        newState = new TilesState();
    }

    public void undo(){
        oldState.load();
        updateRenderer();
    }

    public void redo(){
        newState.load();
        updateRenderer();
    }

    public void updateRenderer(){
        updatedTiles.each((x, y) -> {
            editor.updateRendererBlock(x, y);
            editor.updateRendererStatic(x, y);
        });
    }

    public void setUpdated(Tile tile){
        updatedTiles.set((int)tile.x, (int)tile.y);
    }

    public static Object getTileData(TileData type, Tile tile){
        return switch(type){
            case floor -> tile.floor();
            case overlay -> tile.overlay();
            case block -> tile.block();
            case team -> tile.team();
            case rotation -> (tile.build == null ? 0 : tile.build.rotation);
            case data -> TileData.packMergedData(tile.data, tile.overlayData, tile.floorData);
            case extraData -> tile.extraData;
        };
    }

    // If you call this method with the wrong data type - it is your fault.
    public static void setTileData(TileData type, Tile tile, Object data){
        switch(type){
            case floor -> tile.setFloor((Floor)data);
            case overlay -> tile.setOverlay((Floor)data);
            case block -> tile.setBlock((Block)data, tile.team(), tile.build == null ? 0 : tile.build.rotation);
            case team -> tile.setTeam((Team)data);
            case rotation -> {
                if(tile.build != null) tile.build.rotation = (int)data;
            }
            case data -> {
                int mergedData = (int)data;
                tile.floorData = TileData.unpackFloorData(mergedData);
                tile.overlayData = TileData.unpackOverlayData(mergedData);
                tile.data = TileData.unpackBlockData(mergedData);
            }
            case extraData -> tile.extraData = (int)data;
        }
    }

    public void addTileChange(TileData type, Tile tile, Object oldData, Object newData){
        oldState.addData(type, tile, oldData);
        newState.addData(type, tile, newData);
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
            // TODO: maybe refactor
            allMaps.add(floors);
            allMaps.add(overlays);
            allMaps.add(blocks);
            allMaps.add(teams);
            allMaps.add(rotations);
            allMaps.add(datas);
            allMaps.add(extraDatas);
        }

        public void addData(TileData type, Tile tile, Object data){
            addData(type, (int)tile.x, (int)tile.y, data);
        }

        // TODO: maybe simple Ctrl+C Ctrl+V switch is still better...
        public void addData(TileData type, int x, int y, Object data){
            int index = type.ordinal();
            Class<?> expectedType = dataTypeClasses.get(index);
            if(!expectedType.isInstance(data)) return;

            Object casted = expectedType.cast(data);

            ObjectMap<Object, ChunkedGridBits> map = (ObjectMap<Object, ChunkedGridBits>) allMaps.get(type.ordinal());

            // TODO: equals?
            if(!map.containsKey(casted)){
                map.put(casted, new ChunkedGridBits());
            }
            map.get(casted).set(x, y);
        }

        public void removeData(TileData type, int x, int y){
            int index = type.ordinal();
            for(var grid : allMaps.get(index).values()){
                grid.set(x, y, false);
            }
        }

        public void loadTile(Tile tile){
            for(TileData type : TileData.values()){
                for(ObjectMap.Entry<?, ChunkedGridBits> entry : allMaps.get(type.ordinal())){
                    if(entry.value.get(tile.x, tile.y)){
                        setTileData(type, tile, entry.key);
                    }
                }
            }
        }

        public void load(){
            for(TileData type : TileData.values()){
                for(ObjectMap.Entry<?, ChunkedGridBits> entry : allMaps.get(type.ordinal())){
                    entry.value.each((x, y) -> {
                        setTileData(type, world.tiles.get(x, y), entry.key);
                    });
                }
            }
        }
    }
}