package mu.editor.blocks.operations;

import mindustry.world.*;
import mu.editor.*;

public interface BlocksOperation extends EditorOperation{
    public void start();

    public void stepStart();

    public void act(Tile tile);

    public void stepEnd();

    public void end();
}