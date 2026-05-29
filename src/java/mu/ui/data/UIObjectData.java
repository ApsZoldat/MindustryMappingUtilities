package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.util.*;
import mu.ui.dialogs.*;
import mu.utils.MUAnnotations.*;

import static mu.EditorVars.jsManager;

public abstract class UIObjectData implements JsonSerializable{
    public @NoJson @NoCopy UIObjectData parent = null;
    public @NoJson @NoCopy Object object = null;
    public @NoCopy String buildScript = null;
    public @NoCopy String name;

    public boolean isElementData(){
        return true;
    }

    public abstract Object build();

    public abstract Object buildPreview(UIExplorerDialog dialog);

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();
        table.add("JS Script (On Object Build)").padTop(10f).padBottom(2f).center().row();
        table.field(buildScript, v -> dialog.currentGroup.each(b -> b.buildScript = v)).size(400f, 300f).padBottom(10f).row();
        return table;
    }

    public void runScript(String script){
        if(script == null) return;
        jsManager.setVar("thisData", this);
        jsManager.run(script);
    }

    @Override
    public void write(Json json){
        // Only write fields without NoJson
        for(var fieldMeta : json.getFields(this.getClass()).values()){
            if(!fieldMeta.field.isAnnotationPresent(NoJson.class)){
                json.writeField(this, fieldMeta.field.getName());
            }
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        // Remove NoJson fields from data
        for(var fieldMeta : json.getFields(this.getClass()).values()){
            if(fieldMeta.field.isAnnotationPresent(NoJson.class)){
                jsonData.remove(fieldMeta.field.getName());
            }
        }
        json.readFields(this, jsonData);
    }
}