package mu.editor.blocks.tools;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.world.*;
import mu.editor.blocks.*;
import mu.editor.blocks.brushes.*;
import mu.editor.blocks.operations.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksBrushTool implements BlocksTool, JsonSerializable{
    public ObjectMap<String, BlocksBrush> brushes = new ObjectMap<>();
    public transient RectBrush rectBrush = new RectBrush();
    public transient BlocksBrush brush;
    public transient BlocksOperation operation;

    public int brushWidth = 3, brushHeight = 3;

    public BlocksBrushTool(){
        this.brushes.put("rect", rectBrush);
        setBrush("rect");
    }

    public void setBrush(String name){
        BlocksBrush brush = brushes.get(name);
        if(brush == null){
            throw new RuntimeException(Strings.format("BlocksBrush \"@\" is not defined in BlocksBrushTool.brushes", name));
        }
        brush.resize(brushWidth, brushHeight);
        this.brush = brush;
    }

    public void resizeBrush(int size){
        resizeBrush(size, size);
    }

    public void resizeBrush(int width, int height){
        brush.resize(width, height);
        brushWidth = width;
        brushHeight = height;
    }

    public void start(int x, int y){
        operation = editor.blocksMode.getOperation();
        operation.start();
    }

    public void act(int x, int y){
        int shiftX = (int)((brushWidth - 1) / 2);
        int shiftY = (int)((brushHeight - 1) / 2);

        operation.stepStart();
        for(int curX = 0; curX < brushWidth; ++curX){
            for(int curY = 0; curY < brushHeight; ++curY){
                if(!brush.area.get(curX, curY)) continue;

                Tile tile = world.tiles.get(curX + x - shiftX, curY + y - shiftY);

                if(tile == null) continue;
                operation.act(tile);
            }
        }
        operation.stepEnd();
    }

    public void end(int x, int y){
        operation.end();
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("brush", brushes.findKey(brush, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setBrush(jsonData.getString("brush"));
    }
}