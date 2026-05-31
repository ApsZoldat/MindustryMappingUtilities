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

    public Block block;
    public Floor floor;
    public Floor overlay;
    public int rotation;
    public Team team;

    // Blocks mode tools
    public ObjectMap<String, BlocksTool> tools = new ObjectMap<>();
    public BlocksPickTool pickTool = new BlocksPickTool();
    public BlocksBrushTool brushTool = new BlocksBrushTool();
    public BlocksTool tool;
    
    public BlocksMode(){
        this.tools.put("pick", pickTool);
        this.tools.put("brush", brushTool);
        selection = new GridBits(1000, 1000); // TODO: I JUST NEED TO TEST THIS
        setTool("brush");
    }
    

    public void setTool(String name){
        BlocksTool tool = tools.get(name);
        if(tool == null){
            throw new RuntimeException(Strings.format("EditorTool \"@\" is not defined in BlocksMode.tools", name));
        }
        this.tool = tool;
    }

    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        Point2 pos = view.project(x, y);
        tool.act(pos.x, pos.y);
        return true;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        return;
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer){
        if(!tool.isDraggable) return;
        Point2 pos = view.project(x, y);
        tool.act(pos.x, pos.y);
    }
}