package mu.mods;

import arc.Core;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Reflect;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.meta.Env;
import mu.ui.*;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class RulesDialogMod{
    public CustomRulesDialog dialog;
    public Runnable setupFunc = this::setup;
    public Rules rules;

    public BetterBannedContentDialog revealedBlocksDialog;
    public BetterBannedContentDialog<Block> betterBannedBlocks;
    public BetterBannedContentDialog<UnitType> betterBannedUnits;
    public PlanetBackgroundDialog planetBackgroundDialog;

    public BannedContentDialog oldBannedBlocks;
    public BannedContentDialog oldBannedUnits;

    public int currentNumbered = 0;

    public RulesDialogMod(CustomRulesDialog dialog){
        this.dialog = dialog;
        
        revealedBlocksDialog = new BetterBannedContentDialog("@rules.revealedblocks", ContentType.block, b -> true);
        revealedBlocksDialog.isRevealed = true;
        betterBannedBlocks = new BetterBannedContentDialog<>("@bannedblocks", ContentType.block, Block::canBeBuilt);
        betterBannedUnits= new BetterBannedContentDialog<>("@bannedunits", ContentType.unit, u -> !u.isHidden());
        planetBackgroundDialog = new PlanetBackgroundDialog();
        
        oldBannedBlocks = Reflect.get(dialog, "bannedBlocks");
        oldBannedUnits = Reflect.get(dialog, "bannedUnits");
    }

    public void update(){
        if(settings.getBool("mu_rules_mod")){
            enable();
        }else{
            disable();
        }
    }

    public void enable(){
        if(!dialog.additionalSetup.contains(setupFunc)){
            dialog.additionalSetup.add(setupFunc);
        }
        if(settings.getBool("editor_better_content_dialogs")){
            Reflect.set(dialog, "bannedBlocks", betterBannedBlocks);
            Reflect.set(dialog, "bannedUnits", betterBannedUnits);
        }
    }
    
    public void disable(){
        if(dialog.additionalSetup.contains(setupFunc)){
            dialog.additionalSetup.remove(setupFunc);
        }
        Reflect.set(dialog, "bannedBlocks", oldBannedBlocks);
        Reflect.set(dialog, "bannedUnits", oldBannedUnits);
    }

    public void setup(){
        rules = Reflect.get(dialog, "rules");
        if(settings.getBool("editor_hidden_rules")) addHiddenRules();
        if(settings.getBool("editor_revealed_blocks")){
            addRevealedBlocks();
        }
        if(settings.getBool("editor_planet_background")){
            addPlanetBackground();
        }
    }

    private void category(String category){
        dialog.current = dialog.categories.get(dialog.categoryNames.indexOf(category));
    }

    private void addHiddenRules(){
        category("waves");
        dialog.check("@rules.showspawns", b -> rules.showSpawns = b, () -> rules.showSpawns);


        category("resourcesbuilding");
        dialog.check("@rules.ghostblocks", b -> rules.ghostBlocks = b, () -> rules.ghostBlocks);
        dialog.check("@rules.logicunitbuild", b -> rules.logicUnitBuild = b, () -> rules.logicUnitBuild);


        category("unit");
        dialog.check("@rules.possessionallowed", b -> rules.possessionAllowed = b, () -> rules.possessionAllowed);
        dialog.check("@rules.unitpayloadupdate", b -> rules.unitPayloadUpdate = b, () -> rules.unitPayloadUpdate);


        category("enemy");
        dialog.check("@rules.pvpautopause", b -> rules.pvpAutoPause = b, () -> rules.pvpAutoPause);
        dialog.check("@rules.coredestroyclear", b -> rules.coreDestroyClear = b, () -> rules.coreDestroyClear);


        category("environment");
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


        category("teams");
        Table numberedEdit = new Table(); // Numbered teams
        dialog.numberi("@rules.numberedteam", f -> {
            currentNumbered = f;
            updateNumberedEdit(numberedEdit, Team.get(f));
        }, () -> currentNumbered, 0, 255);
        updateNumberedEdit(numberedEdit, Team.get(currentNumbered));
        if(numberedEdit.hasChildren()){
            dialog.current.add(numberedEdit).row();
        }

        dialog.numberi("@rules.playerteam", f -> rules.defaultTeam = Team.get(f), () -> rules.defaultTeam.id, 0, 255);
        dialog.numberi("@rules.enemyteam", f -> rules.waveTeam = Team.get(f), () -> rules.waveTeam.id, 0, 255);

        dialog.category("miscellaneous");
        dialog.check("@rules.cangameover", b -> rules.canGameOver = b, () -> rules.canGameOver);
        if(Core.bundle.get("rules.modename").toLowerCase().contains(dialog.ruleSearch)){
            text(dialog.current, "@rules.modename", value -> rules.modeName = (value.isEmpty() ? null : value), () -> (rules.modeName == null ? "" : rules.modeName));
        }
        if(Core.bundle.get("rules.mission").toLowerCase().contains(dialog.ruleSearch)){
            text(dialog.current, "@rules.mission", value -> rules.mission = (value.isEmpty() ? null : value), () -> (rules.mission == null ? "" : rules.mission));
        }

        addHiddenTeamRules();
    }
    
    private void addRevealedBlocks(){
        category("miscellaneous");
        if(Core.bundle.get("rules.revealedblocks").toLowerCase().contains(dialog.ruleSearch)){
            dialog.ruleInfo(dialog.current.table(table -> {
                table.button("@rules.revealedblocks", () -> revealedBlocksDialog.show(rules.revealedBlocks)).width(300f).left();
                table.left().row();
            }).fillX(), "@rules.revealedblocks");
            dialog.current.row();
        }
    }
    
    private void addPlanetBackground(){
        category("miscellaneous");
        if(Core.bundle.get("rules.planetbackground").toLowerCase().contains(dialog.ruleSearch)){
            dialog.ruleInfo(dialog.current.table(table -> {
                table.button("@rules.planetbackground", () -> planetBackgroundDialog.show(rules)).width(300f).left();
                table.left().row();
            }).fillX(), "@rules.planetbackground");
            dialog.current.row();
        }
    }

    private void updateNumberedEdit(Table edit, Team team){
        edit.clear();
        boolean[] shown = {false};
        Table wasCurrent = dialog.current;
        Rules rules = Reflect.get(dialog, "rules");

        edit.button(team.coloredName(), Icon.downOpen, Styles.togglet, () -> {
            shown[0] = !shown[0];
        }).marginLeft(14f).width(260f).height(55f).update(t -> {
            ((Image)t.getChildren().get(1)).setDrawable(shown[0] ? Icon.upOpen : Icon.downOpen);
            t.setChecked(shown[0]);
        }).left().padBottom(2f).row();

        edit.collapser(c -> {
            c.left().defaults().fillX().left().pad(5);
            dialog.current = c;
            Rules.TeamRule teams = rules.teams.get(team);

            dialog.number("@rules.blockhealthmultiplier", f -> teams.blockHealthMultiplier = f, () -> teams.blockHealthMultiplier);
            dialog.number("@rules.blockdamagemultiplier", f -> teams.blockDamageMultiplier = f, () -> teams.blockDamageMultiplier);

            dialog.check("@rules.rtsai", b -> teams.rtsAi = b, () -> teams.rtsAi, () -> team != rules.defaultTeam);
            dialog.numberi("@rules.rtsminsquadsize", f -> teams.rtsMinSquad = f, () -> teams.rtsMinSquad, () -> teams.rtsAi, 0, 100);
            dialog.numberi("@rules.rtsmaxsquadsize", f -> teams.rtsMaxSquad = f, () -> teams.rtsMaxSquad, () -> teams.rtsAi, 1, 1000);
            dialog.number("@rules.rtsminattackweight", f -> teams.rtsMinWeight = f, () -> teams.rtsMinWeight, () -> teams.rtsAi);

            //disallow on Erekir (this is broken for mods I'm sure, but whatever)
            dialog.check("@rules.buildai", b -> teams.buildAi = b, () -> teams.buildAi, () -> team != rules.defaultTeam && rules.env != Planets.erekir.defaultEnv && !rules.pvp);
            dialog.number("@rules.buildaitier", false, f -> teams.buildAiTier = f, () -> teams.buildAiTier, () -> teams.buildAi && rules.env != Planets.erekir.defaultEnv && !rules.pvp, 0, 1);

            dialog.check("@rules.infiniteresources", b -> teams.infiniteResources = b, () -> teams.infiniteResources);
            dialog.number("@rules.buildspeedmultiplier", f -> teams.buildSpeedMultiplier = f, () -> teams.buildSpeedMultiplier, 0.001f, 50f);

            dialog.number("@rules.unitdamagemultiplier", f -> teams.unitDamageMultiplier = f, () -> teams.unitDamageMultiplier);
            dialog.number("@rules.unitcrashdamagemultiplier", f -> teams.unitCrashDamageMultiplier = f, () -> teams.unitCrashDamageMultiplier);
            dialog.number("@rules.unitbuildspeedmultiplier", f -> teams.unitBuildSpeedMultiplier = f, () -> teams.unitBuildSpeedMultiplier, 0.001f, 50f);
            dialog.number("@rules.unitcostmultiplier", f -> teams.unitCostMultiplier = f, () -> teams.unitCostMultiplier);
            dialog.number("@rules.unithealthmultiplier", f -> teams.unitHealthMultiplier = f, () -> teams.unitHealthMultiplier);

            dialog.check("@rules.cheat", value -> teams.cheat = value, () -> teams.cheat);
            dialog.check("@rules.coresspawnships", value -> teams.aiCoreSpawn = value, () -> teams.aiCoreSpawn);

            if(!dialog.current.hasChildren()){
                edit.clear();
            }

            dialog.current = wasCurrent;
        }, () -> shown[0]).left().growX().row();
    }

    private void addHiddenTeamRules(){
        category("teams");

        int[] i = {0};
        dialog.current.getCells().each(t -> {
            if(i[0] > 5) return;

            if(t.get() instanceof Table){
                ((Table) t.get()).getCells().each(c -> {
                    if(c.get() instanceof Collapser){
                        Rules.TeamRule teams = ((Rules)Reflect.get(dialog, "rules")).teams.get(Team.baseTeams[i[0]]);
                        i[0]++;

                        dialog.current = Reflect.get(c.get(), "table");
                        dialog.check("@rules.cheat", value -> teams.cheat = value, () -> teams.cheat);
                        dialog.check("@rules.coresspawnships", value -> teams.aiCoreSpawn = value, () -> teams.aiCoreSpawn);
                    }
                });
            }
        });
    }

    private void environmentDialog(Rules rules){
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

    private void changeEnv(CheckBox check, int envVar, Rules rules){
        if (check.isChecked()){
            rules.env = rules.env | envVar;
        } else {
            rules.env = rules.env & ~envVar;
        }
    }

    private void envCheck(Table tb, String text, int envVar, String description, Rules rules){
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

    private void text(Table table, String labelText, Cons<String> cons, Prov<String> prov){
        Cell<Table> cell = table.table(t -> {
            t.left();
            t.add(labelText).left().padRight(5);
            t.field(String.valueOf(prov.get()), cons).padRight(100f);
        }).padTop(0);
        dialog.ruleInfo(cell, labelText);
        table.row();
    }
}
