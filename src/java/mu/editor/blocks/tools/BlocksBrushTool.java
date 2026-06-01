package mu.editor.blocks.tools;

import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import mu.editor.blocks.*;
import mu.editor.blocks.brushes.*;

import static mindustry.Vars.world;
import static mu.EditorVars.*;

public class BlocksBrushTool extends BlocksTool{
    public ObjectMap<String, BlocksBrush> brushes = new ObjectMap<>();
    public SquareBrush squareBrush = new SquareBrush();
    public BlocksBrush brush;

    public int brushWidth = 3, brushHeight = 3;

    public BlocksBrushTool(){
        this.brushes.put("square", squareBrush);
        setBrush("square");
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

    public void act(int x, int y){
        int shiftX = (int)((brushWidth - 1) / 2);
        int shiftY = (int)((brushHeight - 1) / 2);

        for(int curX = 0; curX < brushWidth; ++curX){
            for(int curY = 0; curY < brushHeight; ++curY){
                if(!brush.area.get(curX, curY)) continue;

                Tile tile = world.tiles.get(curX + x - shiftX, curY + y - shiftY);

                if(tile == null) continue;
                editor.blocksMode.action.execute(tile);
            }
        }
    }
}