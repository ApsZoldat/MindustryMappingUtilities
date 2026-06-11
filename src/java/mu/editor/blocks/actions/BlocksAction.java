package mu.editor.blocks.actions;

import mindustry.world.*;
import mu.editor.*;

// TODO: docs
public interface BlocksAction{
    public void startAction();

    public void startStep();

    public void act(Tile tile);

    public void endStep();

    public EditorOperation endAction();
}