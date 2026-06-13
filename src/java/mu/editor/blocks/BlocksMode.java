package mu.editor.blocks;

import arc.struct.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.actions.*;
import mu.editor.blocks.tools.*;
import mu.editor.blocks.operations.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksMode extends EditorMode implements JsonSerializable{
    public GridBits selection;  // TODO: Make multiple of them?

    public int lastX, lastY;

    // Blocks mode tools
    public ObjectMap<String, BlocksTool> tools = new ObjectMap<>();
    public transient BlocksPickTool pickTool = new BlocksPickTool();
    public transient BlocksBrushTool brushTool = new BlocksBrushTool();
    public transient BlocksTool tool;

    // Blocks mode actions
    public transient ObjectMap<String, BlocksAction> actions = new ObjectMap<>();

    public BlocksMode(){
        this.tools.put("pick", pickTool);
        this.tools.put("brush", brushTool);

        // TODO: maybe variables for ts
        this.actions.put("select", new BlocksSelectionAction(true));
        this.actions.put("deselect", new BlocksSelectionAction(false));
        this.actions.put("draw", new BlocksDrawAction());
    }

    public void setTool(String name){
        BlocksTool tool = tools.get(name);
        if(tool == null){
            throw new RuntimeException(Strings.format("BlocksTool \"@\" is not defined in BlocksMode.tools", name));
        }
        this.tool = tool;
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
        selection = new GridBits(width, height);
        setTool("brush");
        pickTool.setAction("draw");
        brushTool.setAction("select");
    }

    public void resize(int width, int height, int shiftX, int shiftY){
        int offsetX = (editor.width() - width) / 2 - shiftX;
        int offsetY = (editor.height() - height) / 2 - shiftY;
        GridBits grid = new GridBits(width, height);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int px = offsetX + x;
                int py = offsetY + y;
                grid.set(x, y, selection.get(px, py));
            }
        }
        selection = grid;
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("tool", tools.findKey(tool, true));
        // json.writeValue("action", actions.findKey(action, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setTool(jsonData.getString("tool"));
        // setAction(jsonData.getString("action"));
    }
}