package mu.editor.blocks.brushes;

import arc.struct.*;
import arc.util.*;

public class RectBrush extends BlocksBrush{
    public void resize(int width, int height){
        area = new GridBits(width, height);
        this.width = width;
        this.height = height;
        ((Bits) Reflect.get(this.area, "bits")).set(0, width * height);
    }
}