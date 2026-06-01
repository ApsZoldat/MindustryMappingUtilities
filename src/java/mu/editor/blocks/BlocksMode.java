package mu.editor.blocks;

import arc.struct.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.tools.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksMode extends EditorMode{
    public GridBits selection;

    public Block block = null;
    public Floor floor = null;
    public Floor overlay = null;
    public Team team = null;
    public int rotation = -1;

    public int lastX, lastY;

    // Blocks mode tools
    public ObjectMap<String, BlocksTool> tools = new ObjectMap<>();
    public BlocksPickTool pickTool = new BlocksPickTool();
    public BlocksBrushTool brushTool = new BlocksBrushTool();
    public BlocksTool tool;

    // Blocks mode actions
    public ObjectMap<String, BlocksAction> actions = new ObjectMap<>();
    public BlocksSelectAction selectAction = new BlocksSelectAction();
    public BlocksDeselectAction deselectAction = new BlocksDeselectAction();
    public BlocksDrawAction drawAction = new BlocksDrawAction();
    public BlocksAction action;
    
    public BlocksMode(){
        this.tools.put("pick", pickTool);
        this.tools.put("brush", brushTool);
        
        this.actions.put("select", selectAction);
        this.actions.put("deselect", deselectAction);
        this.actions.put("draw", drawAction);
        
        selection = new GridBits(1000, 1000); // TODO: I JUST NEED TO TEST THIS

        setTool("brush");
        setAction("select");
    }

    public void setTool(String name){
        BlocksTool tool = tools.get(name);
        if(tool == null){
            throw new RuntimeException(Strings.format("BlocksTool \"@\" is not defined in BlocksMode.tools", name));
        }
        this.tool = tool;
    }

    public void setAction(String name){
        BlocksAction action = actions.get(name);
        if(action == null){
            throw new RuntimeException(Strings.format("BlocksAction \"@\" is not defined in BlocksMode.actions", name));
        }
        this.action = action;
    }

    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        Point2 pos = view.project(x, y);
        lastX = pos.x;
        lastY = pos.y;
        tool.act(pos.x, pos.y);
        return true;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        return;
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer){
        if(!tool.isDraggable) return;
        Point2 pos = view.project(x, y);
        Bresenham2.line(lastX, lastY, pos.x, pos.y, (cx, cy) -> tool.act(cx, cy));
        lastX = pos.x;
        lastY = pos.y;
    }

    public interface BlocksAction{
        public void execute(Tile tile);
    }

    public class BlocksSelectAction implements BlocksAction{
        public void execute(Tile tile){
            editor.blocksMode.selection.set(tile.x, tile.y);
        }
    }

    public class BlocksDeselectAction implements BlocksAction{
        public void execute(Tile tile){
            editor.blocksMode.selection.set(tile.x, tile.y, false);
        }
    }

    public class BlocksDrawAction implements BlocksAction{
        public void execute(Tile tile){
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

            
        }
    }
}