package mu.editor.blocks.tools;

public interface BlocksTool{
    public abstract void start(int x, int y);

    public abstract void act(int x, int y);

    public abstract void end(int x, int y);
}