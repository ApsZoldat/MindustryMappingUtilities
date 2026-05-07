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

// yep this is quite literally https://github.com/mnemotechnician/new-console/blob/master/src%2Fnewconsole%2Fui%2FFloatingWidget.java copy (thank you very much Mnemotechnician <3)

public class Window extends Table{
    public Table cont;
    public Button dragger;

    public boolean isDragging = false;
    public float dragOffsetX, dragOffsetY;
    public float padTop, padLeft, padBottom, padRight;
    public static float draggedAlpha = 0.45f;

    public Window(){
        cont = new Table();
        dragger = new Button();

        padTop = padLeft = padBottom = padRight = 20f;

        add(dragger).size(50f);
        // dragger.setBackground(Styles.black3);

        //addChild(cont);
        cont.button("test", Icon.wrench, () -> {}).size(160f, 64f);

        dragOffsetX = 0;
        dragOffsetY = 0;

        dragger.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
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
            color.a = isDragging ? draggedAlpha : 1f;
        });

        // Just clamping the position
        updateDrag(dragOffsetX, dragOffsetY);
    }

    public void updateDrag(float x, float y){
        if(parent == null) return;

        float deltaX = x - dragOffsetX;
        float deltaY = y - dragOffsetY;

        Vec2 currentPos = localToParentCoordinates(Tmp.v1.set(0, 0));

        setPosition(
            Mathf.clamp(currentPos.x + deltaX, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
            Mathf.clamp(currentPos.y + deltaY, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
        );
    }
}
