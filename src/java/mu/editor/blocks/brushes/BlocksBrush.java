package mu.editor.blocks.brushes;

import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.struct.*;

public abstract class BlocksBrush implements JsonSerializable{
    public GridBits area;
    public int width, height;  // TODO: maybe remov this

    public void resize(int size){
        resize(size, size);
    }

    public abstract void resize(int width, int height);

    @Override
    public void write(Json json){
        json.writeFields(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
    }
}