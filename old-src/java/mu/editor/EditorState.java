package mu.editor;

import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.util.*;
import mindustry.io.*;
import mu.editor.*;
import mu.ui.*;
import mu.utils.*;
import mu.EditorVars;

/** This class contains references to all objects representing current map editor state (for serialization) */
public class EditorState implements JsonSerializable{
    public MUMapEditor editor;
    public MUMapView view;
    public EditorUI ui;

    /** Custom user-defined and serialized variables. Put only basic types and serializable objects here. */
    public ObjectMap<String, Object> vars = new ObjectMap<>();

    public EditorState(){
        this.editor = EditorVars.editor;
        this.view = EditorVars.view;
        this.ui = EditorVars.ui;
    }

    public Object getVar(String name){
        return vars.get(name);
    }

    public void setVar(String name, Object value){
        vars.put(name, value);
    }

    @Override
    public void write(Json json){
        json.writeFields(this);
        json.writeValue("editor", editor);
        json.writeValue("view", view);
        json.writeValue("ui", ui);
    }
    
    @Override
    public void read(Json json, JsonValue jsonData){
        // All this just to make sure new instances of state/editor/view/ui don't get created
        if(json.getClass() != MUJson.class){
            throw new RuntimeException("Only call EditorState.read() using MUJson methods");
        }
        json.readField(this, "vars", jsonData);
        json.readField(this, "editor", jsonData);
        json.readField(this, "view", jsonData);
        json.readField(this, "ui", jsonData);
    }
}