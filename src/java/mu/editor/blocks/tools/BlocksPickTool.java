package mu.editor.blocks.tools;

import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.world.*;
import mu.editor.blocks.*;
import mu.editor.blocks.actions.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;
import static mu.EditorVars.dialog;

public class BlocksPickTool implements BlocksTool, JsonSerializable{
    public transient TileData type = TileData.block;
    public transient BlocksDrawAction action;

    public void setDataType(String name){
        type = TileData.valueOf(name);
    }

    public void setAction(String name){
        BlocksAction action = editor.blocksMode.actions.get(name);
        if(action == null){
            throw new RuntimeException(Strings.format("BlocksAction \"@\" is not defined in BlocksMode.actions", name));
        }
        if(!(action instanceof BlocksDrawAction draw)) throw new RuntimeException(Strings.format("BlocksAction \"@\" is not a BlocksDrawAction", name));
        this.action = draw;
    }

    public void start(int x, int y){
        return;
    }

    public void act(int x, int y){
        return;
    }

    public void end(int x, int y){
        Tile tile = world.tiles.get(x, y);

        if(tile == null){
            dialog.showErrorMessage("temp");
            return;
        }

        action.copyData(type, tile);
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("type", type.toString());
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setDataType(jsonData.getString("type"));
    }
}