package mu.editor;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.content.*;
import mindustry.editor.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.maps.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.blocks.*;
import mu.utils.MUAnnotations.*;

import static mindustry.Vars.*;

public class MUMapEditor extends MapEditor implements JsonSerializable{
    public ObjectMap<String, EditorMode> modes = new ObjectMap<>();
    public EditorMode mode;

    public MUMapEditor(){
        this.modes.put("navigation", new NavigationMode());
        this.modes.put("blocks", new BlocksMode());
        setMode("navigation");
    }

    public void setMode(String name){
        EditorMode mode = modes.get(name);
        if(mode == null){
            throw new RuntimeException(Strings.format("EditorMode \"@\" is not defined in MUMapEditor.modes", name));
        }
        this.mode = mode;
    }

    /*
    public void drawBlock(int x, int y, boolean forceOverlay, Boolf<Tile> tester){
        if(drawBlock.isMultiblock()){
            x = Mathf.clamp(x, (drawBlock.size - 1) / 2, width() - drawBlock.size / 2 - 1);
            y = Mathf.clamp(y, (drawBlock.size - 1) / 2, height() - drawBlock.size / 2 - 1);
            if(!hasOverlap(x, y)){
                tile(x, y).setBlock(drawBlock, drawTeam, rotation);
                addTileOp(TileOp.get((short)x, (short)y, DrawOperation.opTeam, (byte)drawTeam.id));
            }
        }else{
            boolean isFloor = drawBlock.isFloor() && drawBlock != Blocks.air;

            Cons<Tile> drawer = tile -> {
                if(!tester.get(tile)) return;
                boolean changed = false;

                boolean didDataOp = false;
                int oldData1 = 0, oldData2 = 0;

                if(drawBlock.saveData || tile.shouldSaveData()){
                    addTileOp(TileOp.get(tile.x, tile.y, DrawOperation.opData, TileOpData.get(tile.data, tile.floorData, tile.overlayData)));
                    addTileOp(TileOp.get(tile.x, tile.y, DrawOperation.opDataExtra, tile.extraData));
                    oldData1 = TileOpData.get(tile.data, tile.floorData, tile.overlayData);
                    oldData2 = tile.extraData;
                    didDataOp = true;
                }

                int preDataOps = ops();

                if(isFloor){
                    if(forceOverlay){
                        tile.setOverlay(drawBlock.asFloor());
                        changed = true;
                    }else{
                        if(!(drawBlock.asFloor().wallOre && !tile.block().solid)){
                            tile.setFloor(drawBlock.asFloor());
                            if(!(tile.overlay() instanceof OverlayFloor) && !drawBlock.asFloor().supportsOverlay){
                                tile.setOverlay(Blocks.air);
                            }
                            changed = true;
                        }
                    }
                }else if(!(tile.block().isMultiblock() && !drawBlock.isMultiblock())){
                    if(drawBlock.rotate && tile.build != null && tile.build.rotation != rotation){
                        addTileOp(TileOp.get(tile.x, tile.y, DrawOperation.opRotation, (byte)rotation));
                    }

                    tile.setBlock(drawBlock, drawTeam, rotation);
                    changed = !drawBlock.synthetic();

                    if(drawBlock.synthetic()){
                        addTileOp(TileOp.get(tile.x, tile.y, DrawOperation.opTeam, (byte)drawTeam.id));
                    }
                }

                if(changed && drawBlock.saveConfig){
                    drawBlock.placeEnded(tile, null, editor.rotation, drawBlock.lastConfig);
                    renderer.updateStatic(tile.x, tile.y);
                }

                //data and block did not change, undo the data ops
                if(didDataOp && ops() == preDataOps && oldData1 == TileOpData.get(tile.data, tile.floorData, tile.overlayData) && oldData2 == tile.extraData){
                    removeLastOps(2);
                }
            };

            if(square){
                drawSquare(x, y, drawer);
            }else{
                drawCircle(x, y, drawer);
            }
        }
    }*/

    @Override
    public void write(Json json){
        json.writeValue("mode", modes.findKey(mode, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        setMode(jsonData.getString("mode"));
    }
}
