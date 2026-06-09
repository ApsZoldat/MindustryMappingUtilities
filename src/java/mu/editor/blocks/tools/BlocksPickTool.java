package mu.editor.blocks.tools;

import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.world.*;
import mu.editor.blocks.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;
import static mu.EditorVars.dialog;

public class BlocksPickTool implements BlocksTool, JsonSerializable{
    public transient TileData data = TileData.block;

    public void setData(String name){
        data = TileData.valueOf(name);
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

        switch(data){
            case block -> editor.blocksMode.block = tile.block();
            case floor -> editor.blocksMode.block = tile.floor();
            case overlay -> editor.blocksMode.overlay = tile.overlay();
            case rotation -> {
                if(tile.build == null){
                    dialog.showErrorMessage("temp");
                }else{
                    editor.blocksMode.rotation = tile.build.rotation;
                }
            }
            case team -> {
                if(tile.build == null){
                    dialog.showErrorMessage("temp");
                }else{
                    editor.blocksMode.team = tile.build.team;
                }
            }
        }
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("data", data.toString());
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setData(jsonData.getString("data"));
    }
}