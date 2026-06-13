package mu.editor.blocks.tools;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.world.*;
import mu.editor.blocks.*;
import mu.editor.blocks.actions.*;
import mu.editor.blocks.brushes.*;
import mu.editor.blocks.operations.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksBrushTool implements BlocksTool, JsonSerializable{
    public ObjectMap<String, BlocksBrush> brushes = new ObjectMap<>();
    public transient RectBrush rectBrush = new RectBrush();
    public transient BlocksBrush brush;
    public transient BlocksAction action;

    public BlocksBrushTool(){
        this.brushes.put("rect", rectBrush);
        setBrush("rect");
    }

    public void setBrush(String name){
        BlocksBrush brush = brushes.get(name);
        if(brush == null){
            throw new RuntimeException(Strings.format("BlocksBrush \"@\" is not defined in BlocksBrushTool.brushes", name));
        }
        this.brush = brush;
    }

    public void setAction(String name){
        BlocksAction action = editor.blocksMode.actions.get(name);
        if(action == null){
            throw new RuntimeException(Strings.format("BlocksAction \"@\" is not defined in BlocksMode.actions", name));
        }
        this.action = action;
    }

    public void resizeBrush(int size){
        resizeBrush(size, size);
    }

    public void resizeBrush(int width, int height){
        brush.resize(width, height);
    }

    public void start(int x, int y){
        action.startAction();
    }

    public void act(int x, int y){
        int shiftX = (int)((brush.width - 1) / 2);
        int shiftY = (int)((brush.height - 1) / 2);

        action.startStep();
        for(int curX = 0; curX < brush.width; ++curX){
            for(int curY = 0; curY < brush.height; ++curY){
                if(!brush.area.get(curX, curY)) continue;

                Tile tile = world.tiles.get(curX + x - shiftX, curY + y - shiftY);

                if(tile == null) continue;
                action.act(tile);
            }
        }
        action.endStep();
    }

    public void end(int x, int y){
        editor.addOperation(action.endAction());
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("brush", brushes.findKey(brush, true));
        // TODO: make this safer
        json.writeValue("action", editor.blocksMode.actions.findKey(action, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setBrush(jsonData.getString("brush"));
        setAction(jsonData.getString("action"));
    }
}