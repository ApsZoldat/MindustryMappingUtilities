package mu.editor.modes;

import arc.input.*;
import arc.scene.event.*;
import arc.math.geom.*;

/** This class combines all the listeners which define how map editor should behave in this mode */
public abstract class EditorMode{
    public boolean mouseMoved(InputEvent event, float x, float y){
        return false;
    }
    
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        return false;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
        return;
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer){
        return;
    }
    
    public boolean pan(float x, float y, float deltaX, float deltaY){
        return false;
    }

    public boolean zoom(float initialDistance, float distance){
        return false;
    }

    public boolean pinch(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2){
        return false;
    }

    public void pinchStop(){
        return;
    }
}