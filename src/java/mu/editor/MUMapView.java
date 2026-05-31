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
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.*;
import mindustry.editor.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.world.*;
import mu.editor.blocks.*;
import mu.utils.*;
import mu.utils.MUAnnotations.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;

public class MUMapView extends MapView implements JsonSerializable{
    // Inner fields, always cast update() after editing these
    public float offsetx, offsety;
    public float zoom = 1f;
    public float mousex, mousey;

    public Vec2 vec;

    public MUMapView(){
        super();

        vec = new Vec2();

        // Remove all listeners that previous constructor made
        ((DelayedRemovalSeq<EventListener>)Reflect.get(Element.class, this, "listeners")).clear();

        addListener(new InputListener(){
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y){
                mousex = x;
                mousey = y;
                update();
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

    public void update(){
        MUReflect.copyChildFields(this, MapView.class);
    }

    public Vec2 unproject(int x, int y){
        float ratio = 1f / ((float)editor.width() / editor.height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float px = ((float)x / editor.width()) * sclwidth + offsetx * zoom - sclwidth / 2 + getWidth() / 2;
        float py = ((float)(y) / editor.height()) * sclheight
        + offsety * zoom - sclheight / 2 + getHeight() / 2;
        return vec.set(px, py);
    }

    @Override
    public void draw(){
        float ratio = 1f / ((float)editor.width() / editor.height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float centerx = x + width / 2 + offsetx * zoom;
        float centery = y + height / 2 + offsety * zoom;
        float scaling = zoom * Math.min(width, height) / editor.width();

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

        for(Tile tile : world.tiles){
            if(editor.blocksMode.selection.get(tile.x, tile.y)){
                Vec2 v = unproject(tile.x, tile.y).add(x, y);
                Draw.rect(Core.atlas.white(), v.x + scaling/2f, v.y + scaling/2f, scaling, scaling);
            }
        }

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

        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(2f));

        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(3f));
        Lines.rect(x, y, width, height);
        Draw.reset();

        ScissorStack.pop();
    }

    public void clampZoom(){
        zoom = Mathf.clamp(zoom, 0.2f, 20f);
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

    @Override
    public void write(Json json){
        json.writeValue("offsetx", offsetx);
        json.writeValue("offsety", offsety);
        json.writeValue("zoom", zoom);
        json.writeValue("mousex", mousex);
        json.writeValue("mousey", mousey);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        offsetx = jsonData.getFloat("offsetx", 0f);
        offsety = jsonData.getFloat("offsety", 0f);
        zoom = jsonData.getFloat("zoom", 1f);
        mousex = jsonData.getFloat("mousex", 0f);
        mousey = jsonData.getFloat("mousey", 0f);
        update();
    }
}
