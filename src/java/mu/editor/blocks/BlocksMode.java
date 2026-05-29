package mu.editor.blocks;

import arc.struct.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.*;
import mu.editor.blocks.tools.*;

import static mu.EditorVars.*;

public class BlocksMode extends EditorMode{
    public GridBits selection;
    public Block block;
    public Floor floor;
    public Floor overlay;
    public int rotation;
    public Team team;

    public BlocksTool tool = new BlocksPickTool(this);

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