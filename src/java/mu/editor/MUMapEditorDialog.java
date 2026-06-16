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
import mu.utils.*;
import mu.EditorVars;

import static mindustry.Vars.*;

public class MUMapEditorDialog extends MapEditorDialog{
    public MUMapView view;  // shadows private view
    public BaseDialog menu;  // shadows private menu
    public BaseDialog modMenu;
    public UIExplorerDialog explorer;

    public MUMapEditorDialog(){
        super();

        // Remove all listeners that previous constructor made
        // ((DelayedRemovalSeq<EventListener>)Reflect.get(Element.class, this, "listeners")).clear();
        // currently not used

        view = EditorVars.view;
        Reflect.set(MapEditorDialog.class, this, "view", view);
        menu = Reflect.get(MapEditorDialog.class, this, "menu");

        modMenu = new BaseDialog("temp");
        modMenu.addCloseButton();
        explorer = new UIExplorerDialog();
        modMenu.cont.button("temp", Icon.wrench, () -> {
            modMenu.hide();
            explorer.show();
        }).size(180f * 2f + 10, 60f);
    }

    @Override
    public void build(){
        clearChildren();
        EditorVars.ui.build();
        Table buttons = new Table();
        buttons.button(Icon.menu, Styles.squarei, menu::show).size(50f);
        buttons.button(Icon.wrench, Styles.squarei, modMenu::show).size(50f);
        buttons.align(Align.topLeft);
        buttons.marginLeft(3f).marginTop(3f);
        stack(view, EditorVars.ui.windows, buttons).grow();
    }

    @Override
    public @Nullable Map save(){
        boolean isEditor = state.rules.editor;
        state.rules.editor = false;
        state.rules.allowEditRules = false;
        state.rules.objectiveFlags.clear();
        state.rules.objectives.each(MapObjective::reset);
        state.stats = new GameStats();
        String name = editor.tags.get("name", "").trim();
        editor.tags.put("rules", JsonIO.write(state.rules));
        editor.tags.remove("width");
        editor.tags.remove("height");

        player.clearUnit();

        // Remove player unit
        Unit unit = Groups.unit.find(u -> u.spawnedByCore);
        if(unit != null){
            unit.remove();
        }

        Map returned = null;

        if(name.isEmpty()){
            MapInfoDialog info = Reflect.get(MapEditorDialog.class, this, "infoDialog");
            info.show();
            Core.app.post(() -> ui.showErrorMessage("@editor.save.noname"));
        }else{
            Map map = maps.all().find(m -> m.name().equalsIgnoreCase(name));
            if(map != null && !map.custom && !map.workshop){
                handleSaveBuiltin(map);
            }else{
                boolean workshop = false;
                // Try to preserve Steam ID
                if(map != null && map.tags.containsKey("steamid")){
                    editor.tags.put("steamid", map.tags.get("steamid"));
                    workshop = true;
                }
                returned = maps.saveMap(editor.tags);
                MUFiles.moveMapToFolder(returned);
                if(workshop){
                    returned.workshop = workshop;
                }
                ui.showInfoFade("@editor.saved");
            }
        }

        menu.hide();
        Reflect.set(MapEditorDialog.class, this, "saved", true);
        state.rules.editor = isEditor;
        return returned;
    }

    public void showErrorMessage(String text){
        showErrorMessage(text, null);
    }

    public void showErrorMessage(String text, String subtext){
        return;  // TODO: to be implemented
    }
}
