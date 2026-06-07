package mu.editor;

import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;

import static mu.EditorVars.*;

public class NavigationMode extends EditorMode implements JsonSerializable{
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY){
        if(!view.isActive()) return false;
        view.offsetx += (deltaX / view.zoom);
        view.offsety += (deltaY / view.zoom);
        view.update();
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance){
        if(!view.isActive()) return false;
        float nzoom = distance - initialDistance;
        view.zoom += (nzoom / 10000f / Scl.scl(1f) * view.zoom);
        view.clampZoom();
        view.update();
        return false;
    }

    // There's literally no fields but ok ig
    @Override
    public void write(Json json){
        json.writeFields(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
    }
}