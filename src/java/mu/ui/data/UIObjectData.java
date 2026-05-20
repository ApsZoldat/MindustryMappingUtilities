package mu.ui.data;

import arc.scene.ui.layout.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mu.ui.dialogs.*;
import mu.utils.MUAnnotations.*;

public abstract class UIObjectData implements JsonSerializable{
    public abstract Table explorerSettings(UIExplorerDialog dialog);

    public abstract void replaceChild(UIObjectData oldData, UIObjectData newData);

    @Override
    public void write(Json json){
        json.writeFields(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readFields(this, jsonData);
    }
}