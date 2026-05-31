package mu.editor.blocks.brushes;

import arc.struct.*;

public abstract class BlocksBrush{
    public GridBits area;
    public int width, height;  // TODO: maybe remov this

    public void resize(int size){
        resize(size, size);
    }

    public abstract void resize(int width, int height);
}