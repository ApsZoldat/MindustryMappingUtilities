package mu.ui;

import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.data.*;

// yep this is quite literally https://github.com/mnemotechnician/new-console/blob/master/src%2Fnewconsole%2Fui%2FFloatingWidget.java copy (thank you very much Mnemotechnician <3)

public class Window extends Table{
    public WindowData data;

    public String name;

    public Table dragger;
    public Table cont;

    public boolean isDraggable;
    public boolean isDragging = false;
    public float dragOffsetX = 0f, dragOffsetY = 0f;

    public Window(WindowData data){
        this.data = data;
        this.isDraggable = data.isDraggable;
        
        dragger = new Table();
        cont = data.cont.build();

        dragger.setBackground(Styles.black3);
        dragger.touchable = Touchable.enabled;

        cont = data.cont.build();
        
        dragger.add(cont);
        add(dragger);

        dragger.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(event.targetActor != dragger || !isDraggable) return false;
                dragOffsetX = x;
                dragOffsetY = y;
                isDragging = true;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){;
                updateDrag(x, y);
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button){
                isDragging = false;
            }
        });

        update(() -> {
            color.a = isDragging ? data.draggedAlpha : 1f;

            // Just clamping the position
            setPos(data.x, data.y);  // TODO: Only do this once somehow
        });
    }

    public void updateDrag(float x, float y){
        if(parent == null) return;

        float deltaX = x - dragOffsetX;
        float deltaY = y - dragOffsetY;

        Vec2 currentPos = localToParentCoordinates(Tmp.v1.set(0, 0));

        setPos(currentPos.x + deltaX, currentPos.y + deltaY);
    }

    public void setPos(float x, float y){
        setPosition(
            Mathf.clamp(x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
            Mathf.clamp(y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
        );

        data.x = Mathf.clamp(x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2);
        data.y = Mathf.clamp(y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2);
    }
}
