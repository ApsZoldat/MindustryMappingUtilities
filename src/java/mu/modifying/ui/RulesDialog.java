package mu.modifying.ui;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.world.meta.Env;

import static arc.Core.settings;
import static mindustry.Vars.ui;
import static mu.MUVars.planetBackgroundDialog;
import static mu.MUVars.revealedBlocksDialog;

public class RulesDialog{
    public static void modify(CustomRulesDialog dialog){
        dialog.additionalSetup.add(() -> setup(dialog));
    }

    public static void setup(CustomRulesDialog dialog){
        if(settings.getBool("editor_hidden_rules")) addHiddenRules(dialog);
    }

    private static void category(CustomRulesDialog dialog, String category){
        dialog.current = dialog.categories.get(dialog.categoryNames.indexOf(category));
    }

    private static void addHiddenRules(CustomRulesDialog dialog){
        Rules rules = Reflect.get(dialog, "rules");

        category(dialog, "waves");
        dialog.check("@rules.showspawns", b -> rules.showSpawns = b, () -> rules.showSpawns);


        category(dialog, "resourcesbuilding");
        dialog.check("@rules.ghostblocks", b -> rules.ghostBlocks = b, () -> rules.ghostBlocks);
        dialog.check("@rules.logicunitbuild", b -> rules.logicUnitBuild = b, () -> rules.logicUnitBuild);


        category(dialog, "unit");
        dialog.check("@rules.possessionallowed", b -> rules.possessionAllowed = b, () -> rules.possessionAllowed);
        dialog.check("@rules.unitammo", b -> rules.unitAmmo = b, () -> rules.unitAmmo);
        dialog.check("@rules.unitpayloadupdate", b -> rules.unitPayloadUpdate = b, () -> rules.unitPayloadUpdate);


        category(dialog, "enemy");
        dialog.check("@rules.pvpautopause", b -> rules.pvpAutoPause = b, () -> rules.pvpAutoPause);
        dialog.check("@rules.coredestroyclear", b -> rules.coreDestroyClear = b, () -> rules.coreDestroyClear);


        category(dialog, "environment");
        dialog.check("@rules.borderdarkness", b -> rules.borderDarkness = b, () -> rules.borderDarkness);
        dialog.check("@rules.disableoutsidearea", b -> rules.disableOutsideArea = b, () -> rules.disableOutsideArea);
        dialog.check("@rules.staticfog", b -> rules.staticFog = b, () -> rules.staticFog);

        if(Core.bundle.get("rules.staticfogcolor").toLowerCase().contains(dialog.ruleSearch)){
            dialog.current.button(b -> {
                b.left();
                b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                    update(() -> setColor(rules.staticColor));
                }}).grow()).margin(4).size(50f).padRight(10);
                b.add("@rules.staticfogcolor");
            }, () -> ui.picker.show(rules.staticColor, rules.staticColor::set)).left().width(300f).row();
        }
        if(Core.bundle.get("rules.dynamicfogcolor").toLowerCase().contains(dialog.ruleSearch)){
            dialog.current.button(b -> {
                b.left();
                b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                    update(() -> setColor(rules.dynamicColor));
                }}).grow()).margin(4).size(50f).padRight(10);
                b.add("@rules.dynamicfogcolor");
            }, () -> ui.picker.show(rules.dynamicColor, rules.dynamicColor::set)).left().width(300f).row();
        }
        if(Core.bundle.get("rules.cloudscolor").toLowerCase().contains(dialog.ruleSearch)){
            dialog.current.button(b -> {
                b.left();
                b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                    update(() -> setColor(rules.cloudColor));
                }}).grow()).margin(4).size(50f).padRight(10);
                b.add("@rules.cloudscolor");
            }, () -> ui.picker.show(rules.cloudColor, rules.cloudColor::set)).left().width(300f).row();
        }

        dialog.number("@rules.dragmultiplier", f -> rules.dragMultiplier = f, () -> rules.dragMultiplier);

        if(Core.bundle.get("rules.environmentsettings").toLowerCase().contains(dialog.ruleSearch) && settings.getBool("editor_environment_settings")){
            dialog.current.button("@rules.environmentsettings", () -> environmentDialog(rules)).left().width(300f).fillX().row();
        }


        dialog.category("miscellaneous");
        dialog.check("@rules.cangameover", b -> rules.canGameOver = b, () -> rules.canGameOver);
        if(Core.bundle.get("rules.modename").toLowerCase().contains(dialog.ruleSearch)){
            text(dialog, dialog.current, "@rules.modename", value -> rules.modeName = (value.isEmpty() ? null : value), () -> (rules.modeName == null ? "" : rules.modeName));
        }
        if(Core.bundle.get("rules.mission").toLowerCase().contains(dialog.ruleSearch)){
            text(dialog, dialog.current, "@rules.mission", value -> rules.mission = (value.isEmpty() ? null : value), () -> (rules.mission == null ? "" : rules.mission));
        }

        if(Core.bundle.get("rules.revealedblocks").toLowerCase().contains(dialog.ruleSearch) && settings.getBool("editor_revealed_blocks")){
            dialog.ruleInfo(dialog.current.table(table -> {
                table.button("@rules.revealedblocks", () -> revealedBlocksDialog.show(rules.revealedBlocks)).width(300f).left();
                table.left().row();
            }).fillX(), "@rules.revealedblocks");
            dialog.current.row();
        }
        if(Core.bundle.get("rules.planetbackground").toLowerCase().contains(dialog.ruleSearch) && settings.getBool("editor_planet_background")){
            dialog.ruleInfo(dialog.current.table(table -> {
                table.button("@rules.planetbackground", () -> planetBackgroundDialog.show(rules)).width(300f).left();
                table.left().row();
            }).fillX(), "@rules.planetbackground");
            dialog.current.row();
        }

        // TODO: PR so rule search detects these
        addHiddenTeamRules(dialog);
    }

    private static void addHiddenTeamRules(CustomRulesDialog dialog){
        category(dialog, "teams");

        int[] i = {0};
        dialog.current.getCells().each(t -> {
            if(t.get() instanceof Table){
                ((Table) t.get()).getCells().each(c -> {
                    if(c.get() instanceof Collapser){
                        Rules.TeamRule teams = ((Rules)Reflect.get(dialog, "rules")).teams.get(Team.baseTeams[i[0]]);
                        i[0]++;

                        dialog.current = Reflect.get(c.get(), "table");
                        dialog.check("@rules.cheat", value -> teams.cheat = value, () -> teams.cheat);
                        dialog.check("@rules.coresspawnships", value -> teams.aiCoreSpawn = value, () -> teams.aiCoreSpawn);
                        dialog.check("@rules.infiniteammo", value -> teams.infiniteAmmo = value, () -> teams.infiniteAmmo);
                    }
                });
            }
        });
    }

    private static void environmentDialog(Rules rules){
        BaseDialog dialog = new BaseDialog("@rules.title.environment");
        dialog.cont.add("@rules.env.warning").color(Pal.accent).center().padBottom(20f).row();
        dialog.cont.pane(table -> {
            table.left().defaults().growX().left().pad(5);

            table.row();

            envCheck(table, "@rules.env.terrestrial", Env.terrestrial, "@rules.env.terrestrial.description", rules);
            envCheck(table, "@rules.env.space", Env.space, "@rules.env.space.description", rules);
            envCheck(table, "@rules.env.underwater", Env.underwater, "@rules.env.underwater.description", rules);
            envCheck(table, "@rules.env.spores", Env.spores, "@rules.env.spores.description", rules);
            envCheck(table, "@rules.env.scorching", Env.scorching, "@rules.env.scorching.description", rules);
            envCheck(table, "@rules.env.groundOil", Env.groundOil, "@rules.env.groundOil.description", rules);
            envCheck(table, "@rules.env.groundWater", Env.groundWater, "@rules.env.groundWater.description", rules);
            envCheck(table, "@rules.env.oxygen", Env.oxygen, "@rules.env.oxygen.description", rules);
        }).fillX();

        dialog.addCloseButton();

        dialog.show();
    }

    private static void changeEnv(CheckBox check, int envVar, Rules rules){
        if (check.isChecked()){
            rules.env = rules.env | envVar;
        } else {
            rules.env = rules.env & ~envVar;
        }
    }

    private static void envCheck(Table tb, String text, int envVar, String description, Rules rules){
        CheckBox check = new CheckBox(text);
        check.changed(() -> changeEnv(check, envVar, rules));
        check.setChecked((rules.env & envVar) != 0);
        check.left();
        tb.add(check);
        tb.row();

        Cell<Label> desc = tb.add(description);
        desc.get().setWidth(600f);
        desc.get().setWrap(true);
        tb.row();
    }

    private static void text(CustomRulesDialog dialog, Table table, String labelText, Cons<String> cons, Prov<String> prov){
        Cell<Table> cell = table.table(t -> {
            t.left();
            t.add(labelText).left().padRight(5);
            t.field(String.valueOf(prov.get()), cons).padRight(100f);
        }).padTop(0);
        dialog.ruleInfo(cell, labelText);
        table.row();
    }
}
