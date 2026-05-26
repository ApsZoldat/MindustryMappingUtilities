package mu.editor.blocks.tools;

public abstract class BlocksTool{
    public boolean isDraggable = true;

    public abstract void act(int x, int y);
}