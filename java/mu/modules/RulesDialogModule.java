package mu.modules;

import arc.func.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;
import mindustry.world.meta.*;

import static arc.Core.*;

/** Adds extra functionality to every rules dialog in the game. */
public class RulesDialogModule extends MUModule{
    public RulesDialogModule(){
        super("mu_rules_dialog", true);
    }

    @Override
    public void init(){
        Seq<CustomRulesDialog> hooked = new Seq<>();

        hookDialog(hooked, "editor map info", () -> findDialog(Vars.ui.editor, "infoDialog", "ruleInfo"));
        hookDialog(hooked, "editor playtest", () -> findDialog(Vars.ui.editor, "playtestDialog", "dialog"));
        hookDialog(hooked, "custom game", () -> findDialog(Vars.ui.custom, "dialog", "dialog"));
        hookDialog(hooked, "pause menu", () -> findDialog(Vars.ui.paused, "rulesDialog"));

        if(hooked.size == 0){
            throw new RuntimeException("No rules dialogs found");
        }

        Log.info("[MU] Hooked " + hooked.size + " rules dialogs");
    }

    /** Walks a reflection path from a dialog root to a rules dialog; a renamed field throws here, at init. */
    static CustomRulesDialog findDialog(Object root, String... path){
        Object out = root;
        for(String name : path){
            out = Reflect.get(out, name);
        }
        return (CustomRulesDialog)out;
    }

    /** Resolves one dialog and registers this dialog's rebuild closure; discovery failures log and skip that dialog only. */
    void hookDialog(Seq<CustomRulesDialog> out, String name, Prov<CustomRulesDialog> finder){
        try{
            CustomRulesDialog dialog = finder.get();
            if(dialog == null) throw new NullPointerException("dialog is null");
            if(out.contains(dialog, true)) return;

            boolean[] failed = {false};

            //Static list shared by all dialogs: this closure runs on every rebuild of every dialog, so a failure disables only this dialog's rows
            CustomRulesDialog.additionalSetup.add(() -> {
                if(failed[0]) return;
                try{
                    setup(dialog);
                }catch(Throwable t){
                    failed[0] = true;
                    Log.err("[MU] Failed to add hidden rules to " + name + " dialog", t);
                }
            });

            out.add(dialog);
            Log.info("[MU] Hooked " + name + " rules dialog");
        }catch(Throwable t){
            Log.err("[MU] Failed to hook " + name + " rules dialog", t);
        }
    }

    /** Builds this dialog's added rows: the settings gate, two guards, reused vanilla categories, the miscellaneous block, then the team walk. */
    void setup(CustomRulesDialog dialog){
        //Hidden-rules setting, read live so toggling applies on the next rebuild without a restart
        if(!settings.getBool("mu_hidden_rules", true)) return;

        Rules rules = Reflect.get(dialog, "rules");
        //A never-shown dialog has no rules yet, and its closure would still run for other dialogs' rebuilds
        if(rules == null) return;
        //Fresh builds clear categoryNames, so the misc category marks this dialog as already injected
        if(dialog.categoryNames.contains("miscellaneous")) return;

        category(dialog, "waves");
        dialog.check("@rules.hidespawns", b -> rules.hideSpawns = b, () -> rules.hideSpawns);

        category(dialog, "resourcesbuilding");
        dialog.check("@rules.ghostblocks", b -> rules.ghostBlocks = b, () -> rules.ghostBlocks);

        category(dialog, "unit");
        dialog.check("@rules.possessionallowed", b -> rules.possessionAllowed = b, () -> rules.possessionAllowed);
        dialog.check("@rules.unitpayloadupdate", b -> rules.unitPayloadUpdate = b, () -> rules.unitPayloadUpdate);

        category(dialog, "enemy");
        dialog.check("@rules.pvpautopause", b -> rules.pvpAutoPause = b, () -> rules.pvpAutoPause);
        dialog.check("@rules.coredestroyclear", b -> rules.coreDestroyClear = b, () -> rules.coreDestroyClear);

        category(dialog, "environment");
        dialog.check("@rules.borderdarkness", b -> rules.borderDarkness = b, () -> rules.borderDarkness);
        dialog.check("@rules.disableoutsidearea", b -> rules.disableOutsideArea = b, () -> rules.disableOutsideArea);
        dialog.check("@rules.staticfog", b -> rules.staticFog = b, () -> rules.staticFog);
        dialog.number("@rules.dragmultiplier", f -> rules.dragMultiplier = f, () -> rules.dragMultiplier);
        colorRow(dialog, rules.staticColor, "@rules.staticfogcolor");
        colorRow(dialog, rules.dynamicColor, "@rules.dynamicfogcolor");
        colorRow(dialog, rules.cloudColor, "@rules.cloudscolor");
        if(settings.getBool("mu_env_settings", true) && bundle.get("rules.environmentsettings").toLowerCase().contains(dialog.ruleSearch)){
            dialog.current.button("@rules.environmentsettings", () -> envDialog(rules)).left().width(300f).fillX().row();
        }

        category(dialog, "miscellaneous");
        dialog.check("@rules.cangameover", b -> rules.canGameOver = b, () -> rules.canGameOver);
        dialog.text("@rules.modename", s -> rules.modeName = s.isEmpty() ? null : s, () -> rules.modeName == null ? "" : rules.modeName, s -> true, () -> true);
        dialog.text("@rules.mission", s -> rules.mission = s.isEmpty() ? null : s, () -> rules.mission == null ? "" : rules.mission, s -> true, () -> true);

        teamRules(dialog, rules);
    }

    /** Reuses an existing category by name, creating it only when vanilla doesn't have one (miscellaneous). */
    void category(CustomRulesDialog dialog, String name){
        int index = dialog.categoryNames.indexOf(name);
        if(index == -1){
            dialog.category(name);
        }else{
            dialog.current = dialog.categories.get(index);
        }
    }

    /** A color swatch row opening the color picker (vanilla ambientLight pattern), search-gated like the built-in builders. */
    void colorRow(CustomRulesDialog dialog, Color color, String text){
        if(!bundle.get(text.substring(1)).toLowerCase().contains(dialog.ruleSearch)) return;

        dialog.current.button(b -> {
            b.left();
            b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                update(() -> setColor(color));
            }}).grow()).margin(4).size(50f).padRight(10);
            b.add(text);
        }, () -> Vars.ui.picker.show(color, color::set)).left().width(300f).row();
    }

    /** Adds the cheat/ai core spawn checks inside each base team's collapsers, walking the tables vanilla built in its team loop. */
    void teamRules(CustomRulesDialog dialog, Rules rules){
        int index = dialog.categoryNames.indexOf("teams");
        if(index == -1) return;

        Table teams = dialog.categories.get(index);
        Table saved = dialog.current;
        int pos = 0;

        for(Cell<?> cell : teams.getCells()){
            if(!(cell.get() instanceof Table teamTable)) continue;

            Collapser collapser = null;
            String label = null;
            for(Cell<?> c : teamTable.getCells()){
                if(c.get() instanceof Collapser col) collapser = col;
                if(c.get() instanceof TextButton btn && label == null) label = btn.getLabel().getText().toString();
            }
            if(collapser == null || label == null) continue;

            //Search may hide whole team tables, shifting positions; match labels so a skipped team never shifts the rest
            while(pos < Team.baseTeams.length && !Team.baseTeams[pos].coloredName().equals(label)) pos++;
            if(pos >= Team.baseTeams.length) break;

            Rules.TeamRule rule = rules.teams.get(Team.baseTeams[pos]);
            pos++;

            dialog.current = Reflect.get(collapser, "table");
            dialog.check("@rules.cheat", b -> rule.cheat = b, () -> rule.cheat);
            dialog.check("@rules.coresspawnships", b -> rule.aiCoreSpawn = b, () -> rule.aiCoreSpawn);
        }

        dialog.current = saved;
    }

    /** The 8-flag environment sub-dialog; built fresh on every open, so it always reflects the current rules. */
    void envDialog(Rules rules){
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

    /** One checkbox + description row, toggling the immutable {@link Rules#env} set (with an {@code any}-safe rebuild on uncheck). */
    void envCheck(Table table, String text, Env env, String description, Rules rules){
        CheckBox box = new CheckBox(text);
        box.setChecked(rules.env.has(env));
        box.left();
        box.changed(() -> {
            if(box.isChecked()){
                rules.env = rules.env.with(env);
            }else if(rules.env.isAny()){
                //Removing from 'any' throws; rebuild every registered flag instead, minus this one
                Env[] all = Env.all.toArray(Env.class);
                rules.env = Environments.of(all).without(env);
            }else{
                rules.env = rules.env.without(env);
            }
        });
        table.add(box);
        table.row();

        Label label = table.add(description).get();
        label.setWidth(600f);
        label.setWrap(true);
        table.row();
    }
}
