package mu.editor;

import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mu.editor.*;
import mu.ui.*;
import mu.EditorVars;

/** This class contains references to all objects representing current map editor state (for serialization) */
public class EditorState implements JsonSerializable{
    public MUMapEditor editor;
    public MUMapView view;
    public EditorUI ui;
    public ObjectMap<String, Object> vars = new ObjectMap<>();

    public EditorState(){
        this.editor = EditorVars.editor;
        this.view = EditorVars.editorView;
        this.ui = EditorVars.editorUi;
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