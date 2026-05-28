package mu.ui;

import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mu.ui.data.*;

public class EditorUI implements JsonSerializable{
    public Seq<WindowData> windowsData;
    public WidgetGroup windows;

    public EditorUI(){
        windowsData = new Seq<>();
        windowsData.add(new WindowData());
        windows = new WidgetGroup();
        windows.touchable = Touchable.childrenOnly;
    }

    public void build(){
        windows.clear();
        for(WindowData data : windowsData){
            windows.addChild(new Window(data));
        }
    }

    @Override
    public void write(Json json){
        json.writeValue("windowsData", this.windowsData);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        json.readValue(EditorUI.class, jsonData.get("windowsData"));
    }
}