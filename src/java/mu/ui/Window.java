package mu.ui;

import arc.Core;
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

    public Table cont;

    public boolean isDraggable;
    public boolean isDragging = false;
    public float dragOffsetX = 0f, dragOffsetY = 0f;

    public Window(WindowData data){
        this.data = data;
        this.isDraggable = data.isDraggable;
        
        cont = data.cont.build();
        add(cont).grow();
        setBackground(Styles.black3);
        touchable = Touchable.enabled;
        pack();  // i spent like 3 hours to discover ts :sob:

        Window w = this;  // yep stoopid i know

        addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(event.targetActor != w || !isDraggable) return false;
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
        });
        
        // Just clamping the position
        Core.app.post(() -> setPos(data.x, data.y));
        // TODO: maybe make different positions for portrait/landscape modes
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
            Mathf.clamp(x, 0, parent.getWidth() - getPrefWidth()),
            Mathf.clamp(y, 0, parent.getHeight() - getPrefHeight())
        );

        data.x = Mathf.clamp(x, 0, parent.getWidth() - getPrefWidth());
        data.y = Mathf.clamp(y, 0, parent.getHeight() - getPrefHeight());
    }
}
