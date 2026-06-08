package mu.editor;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.content.*;
import mindustry.editor.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.maps.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mu.editor.blocks.*;
import mu.utils.MUAnnotations.*;

import static mindustry.Vars.*;

public class MUMapEditor extends MapEditor implements JsonSerializable{
    // Editor modes
    public ObjectMap<String, EditorMode> modes = new ObjectMap<>();
    public transient NavigationMode navigationMode = new NavigationMode();
    public transient BlocksMode blocksMode = new BlocksMode();
    public transient EditorMode mode;  // only mode name is serialized

    // TODO: Additional onLoad, onSave and onResize Seq<Runnable>

    public MUMapEditor(){
        this.modes.put("navigation", navigationMode);
        this.modes.put("blocks", blocksMode);
        setMode("navigation");
    }

    public void setMode(String name){
        EditorMode mode = modes.get(name);
        if(mode == null){
            throw new RuntimeException(Strings.format("EditorMode \"@\" is not defined in MUMapEditor.modes", name));
        }
        this.mode = mode;
    }

    public void updateRendererBlock(int x, int y){
        Reflect.invoke(this.renderer, "updateBlock", new Object[]{x, y}, int.class, int.class);
    }

    public void updateRendererStatic(int x, int y){
        Reflect.invoke(this.renderer, "updateStatic", new Object[]{x, y}, int.class, int.class);
    }

    @Override
    public void beginEdit(int width, int height){
        super.beginEdit(width, height);
        for(EditorMode mode : modes.values()){
            mode.beginEdit(width, height);
        }
    }

    @Override
    public void beginEdit(Map map){
        super.beginEdit(map);
        for(EditorMode mode : modes.values()){
            mode.beginEdit(width(), height());
        }
    }

    @Override
    public void beginEdit(Pixmap pixmap){
        super.beginEdit(pixmap);
        for(EditorMode mode : modes.values()){
            mode.beginEdit(pixmap.width, pixmap.height);
        }
    }

    @Override
    public void resize(int width, int height, int shiftX, int shiftY){
        super.resize(width, height, shiftX, shiftY);
        for(EditorMode mode : modes.values()){
            mode.resize(width, height, shiftX, shiftY);
        }
    }

    @Override
    public void write(Json json){
        json.writeField(this, "modes");
        json.writeValue("mode", modes.findKey(mode, true));
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
        setMode(jsonData.getString("mode"));
    }
}
