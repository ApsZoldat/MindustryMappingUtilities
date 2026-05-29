package mu.editor.blocks.tools;

import mindustry.world.*;
import mu.editor.blocks.*;

import static mindustry.Vars.*;
import static mu.EditorVars.dialog;

public class BlocksPickTool extends BlocksTool{
    public BlocksMode mode;

    public TileData data = TileData.block;

    public BlocksPickTool(BlocksMode mode){
        this.mode = mode;
        this.isDraggable = false;
    }

    public void act(int x, int y){
        Tile tile = world.tiles.get(x, y);

        if(tile == null){
            dialog.showErrorMessage("temp");
            return;
        }

        switch(data){
            case block -> mode.block = tile.block();
            case floor -> mode.block = tile.floor();
            case overlay -> mode.overlay = tile.overlay();
            case rotation -> {
                if(tile.build == null){
                    dialog.showErrorMessage("temp");
                }else{
                    mode.rotation = tile.build.rotation;
                }
            }
            case team -> {
                if(tile.build == null){
                    dialog.showErrorMessage("temp");
                }else{
                    mode.team = tile.build.team;
                }
            }
        }
    }
}