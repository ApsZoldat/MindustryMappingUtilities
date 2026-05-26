package mu.editor.blocks.tools;

import mindustry.world.*;
import mu.editor.blocks.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editorDialog;

public class BlocksPickTool extends BlocksTool{
    public BlocksMode mode;

    public TileData data = TileData.block;

    public BlocksPickTool(BlocksMode mode){
        this.mode = mode;
        this.isDraggable = false;
    }

    public void act(int x, int y){
        Tile tile = world.tiles.get(x, y);

        switch(data){
            case block -> mode.block = tile.block();
            case floor -> mode.block = tile.floor();
            case overlay -> mode.overlay = tile.overlay();
            case rotation -> {
                if(tile.build == null){
                    editorDialog.showErrorMessage("temp");
                }else{
                    mode.rotation = tile.build.rotation;
                }
            }
            case team -> {
                if(tile.build == null){
                    editorDialog.showErrorMessage("temp");
                }else{
                    mode.team = tile.build.team;
                }
            }
        }
    }
}