package mu.reflect.ui;

import arc.func.*;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.CustomRulesDialog;
import mu.ui.RulesSearchDialog;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mu.MUVars.*;

public class RulesDialog{
    public static void change(CustomRulesDialog dialog){
        dialog.shown(() -> setup(dialog));
    }

    public static void setup(CustomRulesDialog dialog){
        boolean isSearch = (dialog instanceof RulesSearchDialog);

        Rules rules = Reflect.get(CustomRulesDialog.class, dialog, "rules");
        Table main = Reflect.get(CustomRulesDialog.class, dialog, "main");

        if(settings.getBool("editor_rules_search") && !isSearch){
            if(dialog.buttons.find("search") == null){
                String text = bundle.get("search");
                if(text.endsWith(":")) text = text.substring(0, text.length() - 1);
                Prov<Rules> resetter = Reflect.get(CustomRulesDialog.class, dialog, "resetter");
                dialog.buttons.button(text, Icon.zoom, () -> searchDialog.show(rules, resetter)).size(210f, 64f).name("search");
            }
        }
        if(settings.getBool("editor_hidden_rules")){
            Reflect.invoke(dialog, "title", new String[]{"@rules.hidden_rules_general"}, String.class);
            addHiddenRules(main, rules);
        }
        if(settings.getBool("editor_rules_info")) addInfoButtons(main);
    }

    private static void addHiddenRules(Table main, Rules rules) {
        main.defaults().left().growX();
        check(main, "@rules.pvp_auto_pause", value -> rules.pvpAutoPause = value, () -> rules.pvpAutoPause);
        check(main, "@rules.can_game_over", value -> rules.canGameOver = value, () -> rules.canGameOver);
        check(main, "@rules.possession_allowed", value -> rules.possessionAllowed = value, () -> rules.possessionAllowed);
        check(main, "@rules.unit_ammo", value -> rules.unitAmmo = value, () -> rules.unitAmmo);
        check(main, "@rules.unit_payload_update", value -> rules.unitPayloadUpdate = value, () -> rules.unitPayloadUpdate);
        check(main, "@rules.show_spawns", value -> rules.showSpawns = value, () -> rules.showSpawns);
        check(main, "@rules.ghost_blocks", value -> rules.ghostBlocks = value, () -> rules.ghostBlocks);
        check(main, "@rules.logic_unit_build", value -> rules.logicUnitBuild = value, () -> rules.logicUnitBuild);
        check(main, "@rules.core_destroy_clear", value -> rules.coreDestroyClear = value, () -> rules.coreDestroyClear);
        number(main, "@rules.drag_multiplier", value -> rules.dragMultiplier = value, () -> rules.dragMultiplier);
        check(main, "@rules.static_fog", value -> rules.coreDestroyClear = value, () -> rules.coreDestroyClear);

        colorPick(main, "@rules.static_fog_color", rules.staticColor::set, () -> rules.staticColor);
        main.row();
        colorPick(main, "@rules.dynamic_fog_color", rules.dynamicColor::set, () -> rules.dynamicColor);
        main.row();
        main.table(table -> {
            table.left();
            colorPick(table, "@rules.clouds_color", rules.cloudColor::set, () -> rules.dynamicColor);
            table.button(Icon.info, () -> ui.showInfo("[accent]" + bundle.get("rules.clouds_color") + "\n\n[]" + bundle.get("rules.clouds_color.info"))).padLeft(5).fillY().row();
        }).row();

        text(main, "@rules.mode_name", value -> rules.modeName = (value.isEmpty() ? null : value), () -> (rules.modeName == null ? "" : rules.modeName));
        text(main, "@rules.mission", value -> rules.mission = (value.isEmpty() ? null : value), () -> (rules.mission == null ? "" : rules.mission));
        check(main, "@rules.border_darkness", value -> rules.borderDarkness = value, () -> rules.borderDarkness);
        check(main, "@rules.disable_outside_area", value -> rules.disableOutsideArea = value, () -> rules.disableOutsideArea);
        addTeamRules(main, rules);
    }

    private static void addTeamRules(Table main, Rules rules) {
        Seq<Collapser> collapsers = new Seq<>();
        main.getCells().each(cell -> {
            if(cell.get() instanceof Collapser){
                collapsers.add((Collapser)cell.get());
            }
        });

        for(int i = 0; i < Team.baseTeams.length; i++){
            Rules.TeamRule teamRules =  rules.teams.get(Team.baseTeams[i]);

            Table table = Reflect.get(collapsers.get(i), "table");

            table.add("@rules.hidden_rules_team").color(Pal.accent).padTop(20).padRight(100f).padBottom(-3);
            table.row();
            table.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20);
            table.row();

            check(table, "@rules.cheat", value -> teamRules.cheat = value, () -> teamRules.cheat);
            check(table, "@rules.cores_spawn_ships", value -> teamRules.aiCoreSpawn = value, () -> teamRules.aiCoreSpawn);
            check(table, "@rules.infinite_ammo", value -> teamRules.infiniteAmmo = value, () -> teamRules.infiniteAmmo);
        }
    }

    private static void addInfoButtons(Table main){
        main.getCells().each(cell -> {
            var elem = cell.get();

            // Going through all cells in collapser table then
            if(elem instanceof Collapser){
                Table table = Reflect.get(elem, "table");
                table.getCells().each(cell2 -> {
                    var elem2 = cell2.get();

                    if(elem2 instanceof Collapser) return;
                    String labelText = getLabelText(elem2);
                    String infoText = getInfoText(labelText);
                    if(infoText != null) addInfoButton(cell2, infoText);
                });
            }else{
                String labelText = getLabelText(elem);
                String infoText = getInfoText(labelText);
                if(infoText != null) addInfoButton(cell, infoText);
            }
        });
    }

    /* Get label text for a rule element
    Any non-collapser table that has a label inside it is a rule element
    ifelement isn't a rule element, returns null*/
    @Nullable
    public static String getLabelText(Element elem, boolean first){
        if(first && (elem instanceof Label || elem instanceof Collapser)) return null;

        if(elem instanceof Table){
            for(Cell<?> cell : ((Table)elem).getCells()){
                String text = getLabelText(cell.get(), false);
                if(text != null) return text;
            }
        }else if(elem instanceof Label){
            return ((Label)elem).getText().toString();
        }
        return null;
    }

    @Nullable
    public static String getLabelText(Element elem){
        return getLabelText(elem, true);
    }

    /* Gets info string for rule text
    If it isn't present in bundle, returns null*/
    @Nullable
    private static String getInfoText(String text){
        if(text == null) return null;
        String bundleKey = bundle.getProperties().findKey((text), false);
        if(bundleKey == null) return null;

        if(!bundle.has(bundleKey + ".info")) return null;
        return bundleKey;
    }

    private static void addInfoButton(Cell<?> cell, String bundleKey){
        var elem = cell.get();
        Table table = new Table();
        table.left().defaults().fillX().left();

        table.button(Icon.infoSmall, () -> ui.showInfo("[accent]" + bundle.get(bundleKey) + "\n\n[]" + bundle.get(bundleKey + ".info"))).padRight(5);
        table.add(elem).row();

        cell.setElement(table);
    }

    private static void number(Table main, String text, boolean integer, Floatc cons, Floatp prov, Boolp condition, float min, float max){
        main.table(table -> {
            table.left();
            table.add(text).left().padRight(5)
                    .update(a -> a.setColor(condition.get() ? Color.white : Color.gray));
            table.field(String.valueOf(integer ? (int) prov.get() : prov.get()), s -> cons.get(Strings.parseFloat(s)))
                    .padRight(100f)
                    .update(a -> a.setDisabled(!condition.get()))
                    .valid(value -> Strings.canParsePositiveFloat(value) && Strings.parseFloat(value) >= min && Strings.parseFloat(value) <= max).width(120f).left();
        }).padTop(0);
        main.row();
    }

    private static void number(Table main, String text, Floatc cons, Floatp prov){
        number(main, text, false, cons, prov, () -> true, 0, Float.MAX_VALUE);
    }

    private static void check(Table main, String text, Boolc cons, Boolp prov, Boolp condition){
        main.check(text, cons).checked(prov.get()).update(a -> a.setDisabled(!condition.get())).padRight(100f).get().left();
        main.row();
    }
    private static void check(Table main, String text, Boolc cons, Boolp prov){
        check(main, text, cons, prov, () -> true);
    }

    private static void colorPick(Table main, String text, Cons<Color> cons, Prov<Color> prov){
        main.button(button -> {
            button.left();
            button.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                update(() -> setColor(prov.get()));
            }}).grow()).margin(4).size(50f).padRight(10);
            button.add(text);
        }, () -> ui.picker.show(prov.get(), cons)).left().width(250f);
    }

    private static void text(Table main, String labelText, Cons<String> cons, Prov<String> prov){
        main.table(table -> {
            table.left();
            table.add(labelText).left().padRight(5);
            table.field(String.valueOf(prov.get()), cons).padRight(100f);
        }).padTop(0).row();
    }
}
