package mu.editor.blocks;

import arc.struct.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;
import arc.func.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.tools.*;
import mu.editor.blocks.operations.*;
import mu.utils.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksMode extends EditorMode implements JsonSerializable{
    public ChunkedGridBits selection;

    public Block block = null;
    public Floor floor = null;
    public Floor overlay = null;
    public Team team = null;
    public int rotation = -1;

    public int lastX, lastY;

    // Blocks mode tools
    public ObjectMap<String, BlocksTool> tools = new ObjectMap<>();
    public transient BlocksPickTool pickTool = new BlocksPickTool();
    public transient BlocksBrushTool brushTool = new BlocksBrushTool();
    public transient BlocksTool tool;

    // Blocks mode actions (suppliers for new operations)
    public transient ObjectMap<String, Prov<BlocksOperation>> actions = new ObjectMap<>();
    public transient Prov<BlocksOperation> action;

    // Operation stack
    public transient EditorOperationStack<BlocksOperation> operationStack;

    public BlocksMode(){
        this.tools.put("pick", pickTool);
        this.tools.put("brush", brushTool);
        
        this.actions.put("select", () -> new BlocksSelectionOperation());
        this.actions.put("deselect", () -> {
            BlocksSelectionOperation op = new BlocksSelectionOperation();
            op.select = false;
            return op;
        });
        //this.actions.put("draw", drawAction);

        operationStack = new EditorOperationStack<>();

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
        Prov<BlocksOperation> action = actions.get(name);
        if(action == null){
            throw new RuntimeException(Strings.format("BlocksAction \"@\" is not defined in BlocksMode.actions", name));
        }
        this.action = action;
    }

    public BlocksOperation getOperation(){
        BlocksOperation op = action.get();
        operationStack.add(op);
        return op;
    }

    public void undo(){
        operationStack.undo();
    }

    public void redo(){
        operationStack.redo();
    }

    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        // On mobile it must be the first finger touching the screen
        if(pointer != 0) return false;

        Point2 pos = view.project(x, y);
        lastX = pos.x;
        lastY = pos.y;
        tool.start(pos.x, pos.y);
        return true;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        Point2 pos = view.project(x, y);
        tool.end(pos.x, pos.y);
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer){
        Point2 pos = view.project(x, y);
        Bresenham2.line(lastX, lastY, pos.x, pos.y, (cx, cy) -> tool.act(cx, cy));
        lastX = pos.x;
        lastY = pos.y;
    }

    public void beginEdit(int width, int height){
        selection = new ChunkedGridBits();
    }

    public void resize(int width, int height, int shiftX, int shiftY){
        int offsetX = (editor.width() - width) / 2 - shiftX;
        int offsetY = (editor.height() - height) / 2 - shiftY;
        ChunkedGridBits grid = new ChunkedGridBits();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int px = offsetX + x;
                int py = offsetY + y;
                grid.set(x, y, selection.get(px, py));
            }
        }
        selection = grid;
        operationStack.clear();  // TODO: maybe keep it
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

            if(updateBlock) editor.updateRendererBlock(tile.x, tile.y);
            if(updateStatic) editor.updateRendererStatic(tile.x, tile.y);
        }
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("tool", tools.findKey(tool, true));
        json.writeValue("action", actions.findKey(action, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setTool(jsonData.getString("tool"));
        setAction(jsonData.getString("action"));
    }
}