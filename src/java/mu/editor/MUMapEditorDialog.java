package mu.editor;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.game.MapObjectives.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.maps.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.meta.*;
import mu.ui.*;
import mu.ui.dialogs.*;

import static mindustry.Vars.*;
import static mu.EditorVars.editor;
import static mu.EditorVars.editorView;
import static mu.EditorVars.windows;

public class MUMapEditorDialog extends MapEditorDialog{
    public MUMapView view;  // shadows private view
    public BaseDialog menu; // shadows private menu
    public UIExplorerDialog explorer;

    public MUMapEditorDialog(){
        super();

        // Remove all listeners that previous constructor made
        // ((DelayedRemovalSeq<EventListener>)Reflect.get(Element.class, this, "listeners")).clear();
        // currently not used

        view = editorView;
        Reflect.set(MapEditorDialog.class, this, "view", view);
        menu = Reflect.get(MapEditorDialog.class, this, "menu");

        explorer = new UIExplorerDialog();
        menu.cont.row();
        menu.cont.button("temp", Icon.wrench, () -> {
            menu.hide();
            explorer.show();
        }).padTop(20f).size(180f * 2f + 10, 60f);
    }

    @Override
    public void build(){
        float size = mobile ? 50f : 58f;

        clearChildren();
        table(cont -> {
            cont.left();

            cont.table(mid -> {
                mid.top();

                Table tools = new Table().top();

                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                Table[] lastTable = {null};

                Cons<EditorTool> addTool = tool -> {

                    ImageButton button = new ImageButton(ui.getIcon(tool.name()), Styles.squareTogglei);
                    button.clicked(() -> {
                        view.setTool(tool);
                        if(lastTable[0] != null){
                            lastTable[0].remove();
                        }
                    });
                    button.update(() -> button.setChecked(view.getTool() == tool));
                    group.add(button);

                    if(tool.altModes.length > 0){
                        button.clicked(l -> {
                            if(!mobile){
                                //desktop: rightclick
                                l.setButton(KeyCode.mouseRight);
                            }
                        }, e -> {
                            //need to double tap
                            if(mobile && e.getTapCount() < 2){
                                return;
                            }

                            if(lastTable[0] != null){
                                lastTable[0].remove();
                            }

                            Table table = new Table(Styles.black9);
                            table.defaults().size(300f, 70f);

                            for(int i = 0; i < tool.altModes.length; i++){
                                int mode = i;
                                String name = tool.altModes[i];

                                table.button(b -> {
                                    b.left();
                                    b.marginLeft(6);
                                    b.setStyle(Styles.flatTogglet);
                                    b.add(Core.bundle.get("toolmode." + name)).left();
                                    b.row();
                                    b.add(Core.bundle.get("toolmode." + name + ".description")).color(Color.lightGray).left();
                                }, () -> {
                                    tool.mode = (tool.mode == mode ? -1 : mode);
                                    table.remove();
                                }).update(b -> b.setChecked(tool.mode == mode));
                                table.row();
                            }

                            table.update(() -> {
                                Vec2 v = button.localToStageCoordinates(Tmp.v1.setZero());
                                table.setPosition(v.x, v.y, Align.topLeft);
                                if(!isShown()){
                                    table.remove();
                                    lastTable[0] = null;
                                }
                            });

                            table.pack();
                            table.act(Core.graphics.getDeltaTime());

                            addChild(table);
                            lastTable[0] = table;
                        });
                    }


                    Label mode = new Label("");
                    mode.setColor(Pal.remove);
                    mode.update(() -> mode.setText(tool.mode == -1 ? "" : "M" + (tool.mode + 1) + " "));
                    mode.setAlignment(Align.bottomRight, Align.bottomRight);
                    mode.touchable = Touchable.disabled;

                    tools.stack(button, mode);
                };

                tools.defaults().size(size, size);

                tools.button(Icon.menu, Styles.flati, menu::show);

                ImageButton grid = tools.button(Icon.grid, Styles.squareTogglei, () -> view.setGrid(!view.isGrid())).get();

                addTool.get(EditorTool.zoom);

                tools.row();

                ImageButton undo = tools.button(Icon.undo, Styles.flati, editor::undo).get();
                ImageButton redo = tools.button(Icon.redo, Styles.flati, editor::redo).get();

                addTool.get(EditorTool.pick);

                tools.row();

                undo.setDisabled(() -> !editor.canUndo());
                redo.setDisabled(() -> !editor.canRedo());

                undo.update(() -> undo.getImage().setColor(undo.isDisabled() ? Color.gray : Color.white));
                redo.update(() -> redo.getImage().setColor(redo.isDisabled() ? Color.gray : Color.white));
                grid.update(() -> grid.setChecked(view.isGrid()));

                addTool.get(EditorTool.line);
                addTool.get(EditorTool.pencil);
                addTool.get(EditorTool.eraser);

                tools.row();

                addTool.get(EditorTool.fill);
                addTool.get(EditorTool.spray);

                ImageButton rotate = tools.button(Icon.right, Styles.flati, () -> editor.rotation = (editor.rotation + 1) % 4).get();
                rotate.getImage().update(() -> {
                    rotate.getImage().setRotation(editor.rotation * 90);
                    rotate.getImage().setOrigin(Align.center);
                });

                tools.row();

                tools.image(Tex.whiteui, Pal.gray).colspan(3).height(4f).width(size * 3f + 3f).row();

                ButtonGroup<ImageButton> teamgroup = new ButtonGroup<>();

                int i = 0;

                for(Team team : Team.baseTeams){
                    ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
                    button.margin(4f);
                    button.getImageCell().grow();
                    button.getStyle().imageUpColor = team.color;
                    button.clicked(() -> editor.drawTeam = team);
                    button.update(() -> button.setChecked(editor.drawTeam == team));
                    teamgroup.add(button);
                    tools.add(button);

                    if(i++ % 3 == 2) tools.row();
                }

                mid.add(tools).top().padBottom(-6);

                mid.row();

                mid.table(Tex.underline, t -> {
                    Slider slider = new Slider(0, MUMapEditor.brushSizes.length - 1, 1, false);
                    slider.moved(f -> editor.brushSize = MUMapEditor.brushSizes[(int)f]);
                    for(int j = 0; j < MUMapEditor.brushSizes.length; j++){
                        if(MUMapEditor.brushSizes[j] == editor.brushSize){
                            slider.setValue(j);
                        }
                    }

                    var label = new Label("@editor.brush");
                    label.setAlignment(Align.center);
                    label.touchable = Touchable.disabled;

                    t.top().stack(slider, label).width(size * 3f - 20).padTop(4f);
                    t.row();
                }).padTop(5).growX().top();

                mid.row();

                mid.check("@editor.showblocks", editor.showBuildings, b -> {
                    editor.showBuildings = b;
                    Reflect.invoke(
                    editor.renderer, "recacheShadows");
                }).pad(2f).growX().with(Table::left).row();
                mid.check("@editor.showterrain", editor.showTerrain, b -> {
                    editor.showTerrain = b;
                    Reflect.invoke(
                    editor.renderer, "recacheTerrain");
                }).pad(2f).growX().with(Table::left).row();
                mid.check("@editor.showfloor", editor.showFloor, b -> editor.showFloor = b).pad(2f).growX().with(Table::left).row();

                if(!mobile){
                    mid.button("@editor.center", Icon.move, Styles.flatt, view::center).growX().margin(9f);
                }
            }).margin(0).left().growY();

            windows.touchable = Touchable.childrenOnly;
            cont.stack(view, windows).grow();

            cont.table(t -> Reflect.invoke(MapEditorDialog.class, this, "addBlockSelection", new Object[]{t}, Table.class)).right().growY();

        }).grow();
    }

    private void doInput(){
        if(Core.input.ctrl()){
            //alt mode select
            for(int i = 0; i < view.getTool().altModes.length; i++){
                if(i + 1 < KeyCode.numbers.length && Core.input.keyTap(KeyCode.numbers[i + 1])){
                    view.getTool().mode = i;
                }
            }
        }else{
            for(EditorTool tool : EditorTool.all){
                if(Core.input.keyTap(tool.key)){
                    view.setTool(tool);
                    break;
                }
            }
        }

        if(Core.input.keyTap(KeyCode.escape)){
            if(!menu.isShown()){
                menu.show();
            }
        }

        if(Core.input.keyTap(KeyCode.r)){
            editor.rotation = Mathf.mod(editor.rotation + 1, 4);
        }

        if(Core.input.keyTap(KeyCode.e)){
            editor.rotation = Mathf.mod(editor.rotation - 1, 4);
        }

        //ctrl keys (undo, redo, save)
        if(Core.input.ctrl()){
            if(Core.input.keyTap(KeyCode.z)){
                if(Core.input.shift()){
                    editor.redo();
                }else{
                    editor.undo();
                }
            }

            if(Core.input.keyTap(KeyCode.y)){
                editor.redo();
            }

            if(Core.input.keyTap(KeyCode.s)){
                save();
            }

            if(Core.input.keyTap(KeyCode.g)){
                view.setGrid(!view.isGrid());
            }
        }
    }
}
