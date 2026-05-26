package mu.editor.blocks.brushes;

import arc.struct.*;
import arc.util.*;

public class SquareBrush extends BlocksBrush{
    public void resize(int width, int height){
        area = new GridBits(width, height);
        this.width = width;
        this.height = height;
        ((Bits) Reflect.get(this.area, "bits")).set(0, width * height);
        this.shiftX = (int)(width / 2);
        this.shiftY = (int)(height / 2);
    }
}