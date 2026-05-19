package mu.editor.modes;

import arc.scene.ui.layout.*;
import arc.input.*;
import arc.math.geom.*;

import static mu.MUVars.*;

public class NavigationMode extends EditorMode{
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY){
        if(!editorView.isActive()) return false;
        editorView.setField("offsetx", editorView.offsetx + (deltaX / editorView.zoom));
        editorView.setField("offsety", editorView.offsety + (deltaY / editorView.zoom));
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance){
        if(!editorView.isActive()) return false;
        float nzoom = distance - initialDistance;
        editorView.setField("zoom", editorView.zoom + (nzoom / 10000f / Scl.scl(1f) * editorView.zoom));
        editorView.clampZoom();
        return false;
    }
}