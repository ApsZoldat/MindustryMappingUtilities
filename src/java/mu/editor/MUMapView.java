package mu.editor;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.input.GestureDetector.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.editor.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;
import mu.editor.*;
import mu.editor.modes.*;
import mu.utils.MUAnnotations.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;

public class MUMapView extends MapView{
    // Shadow fields - set only through setField()
    public @Shadow float offsetx, offsety;
    public @Shadow float zoom = 1f;
    public @Shadow float mousex, mousey;

    public MUMapView(){
        super();

        // Remove all listeners that previous constructor made
        ((DelayedRemovalSeq<EventListener>)Reflect.get(Element.class, this, "listeners")).clear();

        addListener(new InputListener(){
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y){
                mousex = x;
                mousey = y;
                return editor.mode.mouseMoved(event, x, y);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                requestScroll();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                return editor.mode.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                editor.mode.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                mousex = x;
                mousey = y;
                editor.mode.touchDragged(event, x, y, pointer);
            }
        });
    }

    // Ideally only a temporary solution
    public Object getField(String name){
        try{
            return Reflect.get(this, name);
        }catch (Exception err){
            Log.err("Failed to get field \"" + name + "\" in MUMapView.", err);
            return null;
        }
    }

    public void setField(String name, Object value){
        try{
            Reflect.set(this, name, value);
            if(this.getClass().getDeclaredField(name).isAnnotationPresent(Shadow.class)){
                Reflect.set(MapView.class, this, name, value);
            }
        }catch (Exception err){
            Log.err("Failed to set field \"" + name + "\" in MUMapView.", err);
        }
    }

    @Override
    public void draw(){
        float ratio = 1f / ((float)editor.width() / editor.height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float centerx = x + width / 2 + offsetx * zoom;
        float centery = y + height / 2 + offsety * zoom;

        GridImage image = Reflect.get(MapView.class, this, "image");
        image.setImageSize(editor.width(), editor.height());

        Rect rect = Reflect.get(MapView.class, this, "rect");
        if(!ScissorStack.push(rect.set(x + Core.scene.marginLeft, y + Core.scene.marginBottom, width, height))){
            return;
        }

        Draw.color(Pal.remove);
        Lines.stroke(2f);
        Lines.rect(centerx - sclwidth / 2 - 1, centery - sclheight / 2 - 1, sclwidth + 2, sclheight + 2);
        editor.renderer.draw(centerx - sclwidth / 2 + Core.scene.marginLeft, centery - sclheight / 2 + Core.scene.marginBottom, sclwidth, sclheight);
        Draw.reset();

        /*if(grid){
            Draw.color(Color.gray);
            image.setBounds(centerx - sclwidth / 2, centery - sclheight / 2, sclwidth, sclheight);
            image.draw();

            Lines.stroke(2f);
            Draw.color(Pal.bulletYellowBack);
            Lines.line(centerx - sclwidth/2f, centery - sclheight/4f, centerx + sclwidth/2f, centery - sclheight/4f);
            Lines.line(centerx - sclwidth/4f, centery - sclheight/2f, centerx - sclwidth/4f, centery + sclheight/2f);
            Lines.line(centerx - sclwidth/2f, centery + sclheight/4f, centerx + sclwidth/2f, centery + sclheight/4f);
            Lines.line(centerx + sclwidth/4f, centery - sclheight/2f, centerx + sclwidth/4f, centery + sclheight/2f);

            Lines.stroke(3f);
            Draw.color(Pal.accent);
            Lines.line(centerx - sclwidth/2f, centery, centerx + sclwidth/2f, centery);
            Lines.line(centerx, centery - sclheight/2f, centerx, centery + sclheight/2f);

            Draw.reset();
        }*/

        float scaling = zoom * Math.min(width, height) / editor.width();

        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(2f));

        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(3f));
        Lines.rect(x, y, width, height);
        Draw.reset();

        ScissorStack.pop();
    }

    public void clampZoom(){
        setField("zoom", Mathf.clamp(zoom, 0.2f, 20f));
    }

    public boolean isActive(){
        return Core.scene != null && Core.scene.getKeyboardFocus() != null
        && Core.scene.getKeyboardFocus().isDescendantOf(ui.editor)
        && ui.editor.isShown() && editor.mode instanceof NavigationMode &&
        Core.scene.getHoverElement() == this;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY){
        return editor.mode.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean zoom(float initialDistance, float distance){
        return editor.mode.zoom(initialDistance, distance);
    }

    @Override
    public boolean pinch(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2){
        return editor.mode.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    @Override
    public void pinchStop(){
        editor.mode.pinchStop();
    }
}
