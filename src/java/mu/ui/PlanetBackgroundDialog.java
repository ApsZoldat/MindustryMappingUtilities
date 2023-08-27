package mu.ui;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ScrollPane;
import arc.util.Tmp;
import mindustry.game.Rules;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mu.utils.PlanetBackgroundDrawer;

import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.content;
import static mindustry.Vars.ui;

public class PlanetBackgroundDialog extends BaseDialog{
    private Rules rules;
    private float zoom = 1f;

    public PlanetBackgroundDialog(){
        super("@rules.planet_background", new DialogStyle(){{
            stageBackground = Styles.none;
            titleFont = Fonts.def;
            titleFontColor = Pal.accent;
            // Don't specify background, so it won't darken the planet view
        }});

        dragged((cx, cy) -> {
            if(rules.planetBackground == null) return;
            // No multitouch drag
            if(Core.input.getTouches() > 1) return;

            Vec3 pos = rules.planetBackground.camPos;

            float upV = pos.angle(Vec3.Y);
            float xScale = 9f, yScale = 10f;
            float margin = 1;

            // Scale X speed depending on polar coordinate
            float speed = 1f - Math.abs(upV - 90) / 90f;

            pos.rotate(rules.planetBackground.camUp, cx / xScale * speed);

            // Prevent user from scrolling all the way up and glitching it out
            float amount = cy / yScale;
            amount = Mathf.clamp(upV + amount, margin, 180f - margin) - upV;

            pos.rotate(Tmp.v31.set(rules.planetBackground.camUp).rotate(rules.planetBackground.camDir, 90), amount);
            PlanetBackgroundDrawer.update();
        });

        addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                if(rules.planetBackground == null) return false;
                if(event.targetActor == PlanetBackgroundDialog.this){
                    zoom = Mathf.clamp(zoom + amountY / 10f, rules.planetBackground.planet.minZoom, 50f);
                }
                PlanetBackgroundDrawer.update();
                return true;
            }
        });

        addCaptureListener(new ElementGestureListener(){
            float lastZoom = -1f;

            @Override
            public void zoom(InputEvent event, float initialDistance, float distance){
                if(rules.planetBackground == null) return;
                if(lastZoom < 0){
                    lastZoom = zoom;
                }

                zoom = (Mathf.clamp(initialDistance / distance * lastZoom, rules.planetBackground.planet.minZoom, 50f));
                PlanetBackgroundDrawer.update();
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                lastZoom = zoom;
            }
        });

        addCloseButton();
        buttons.button("@remove_background", Icon.none, () -> ui.showConfirm("@confirm", () -> {
            rules.planetBackground = null;
            build();
        })).get().setDisabled(() -> rules.planetBackground == null);
        buttons.button("@rules.title.planet", Icon.planet, () -> {
            BaseDialog dialog = new BaseDialog("@rules.title.planet");
            dialog.cont.pane(table -> {
                int i = 0;
                for (Planet planet : content.planets()) {
                    table.button(planet.localizedName, Icon.planet, Styles.togglet, () -> {
                        rules.planetBackground.planet = planet;
                        PlanetBackgroundDrawer.update();
                        dialog.hide();
                    }).marginLeft(14f).padBottom(5f).width(220f).height(55f).checked(rules.planetBackground.planet == planet)
                            .update(b -> b.setChecked(rules.planetBackground.planet == planet)).get().getChildren().get(1).setColor(planet.iconColor);
                    i += 1;
                    if (i % 3 == 0) {
                        table.row();
                    }
                }
            });

            dialog.addCloseButton();
            dialog.show();
        }).get().setDisabled(() -> rules.planetBackground == null);

        shown(this::build);
    }

    public void show(Rules rules){
        this.rules = rules;
        show();
    }

    private void build(){
        cont.clear();
        if(rules.planetBackground == null){
            cont.add("@empty").row();
            cont.button("@add", Icon.add, () -> {
                rules.planetBackground = new PlanetParams();
                build();
            }).width(100f);
        }
        PlanetBackgroundDrawer.update();
    }

    @Override
    public void draw(){
        if (rules.planetBackground != null){
            if(scene.getDialog() == PlanetBackgroundDialog.this && !scene.hit(input.mouseX(), input.mouseY(), true).isDescendantOf(e -> e instanceof ScrollPane)){
                scene.setScrollFocus(PlanetBackgroundDialog.this);
            }
            rules.planetBackground.zoom = Mathf.lerpDelta(rules.planetBackground.zoom, zoom, 0.4f);
            float drawSize = Math.max(Core.graphics.getWidth(), Core.graphics.getHeight());
            Draw.rect(Draw.wrap(PlanetBackgroundDrawer.draw(rules.planetBackground)), (float)Core.graphics.getWidth() / 2, (float)Core.graphics.getHeight() / 2, drawSize, -drawSize);
            Draw.flush();
        }else{
            Draw.color(color.r, color.g, color.b, color.a * parentAlpha);
            Styles.black9.draw(x, y, width, height);
        }

        super.draw();
    }
}
