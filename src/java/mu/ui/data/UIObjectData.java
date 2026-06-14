package mu.ui.data;

import arc.struct.*;
import arc.scene.ui.layout.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.util.*;
import mu.ui.dialogs.*;
import mu.utils.MUAnnotations.*;

import static mu.EditorVars.jsManager;

public abstract class UIObjectData implements JsonSerializable{
    public transient @NoCopy UIObjectData parent = null;
    public transient @NoCopy Object object = null;
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
        table.area(buildScript, v -> dialog.currentGroup.each(b -> b.buildScript = v)).size(400f, 300f).padBottom(10f).maxTextLength(Integer.MAX_VALUE).row();
        return table;
    }

    public void runScript(String script){
        if(script == null) return;
        jsManager.setVar("thisData", this);
        jsManager.setVar("thisObject", this.object);
        jsManager.run(script);
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
    }
}