package mu.editor.blocks.tools;

import mindustry.world.*;
import mu.editor.blocks.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;
import static mu.EditorVars.dialog;

public class BlocksPickTool extends BlocksTool{
    public TileData data = TileData.block;

    public BlocksPickTool(){
        this.isDraggable = false;
    }

    public void act(int x, int y){
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
}