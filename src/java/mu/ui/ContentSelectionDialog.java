package mu.ui;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Planet;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

import static arc.Core.settings;
import static mindustry.Vars.content;
import static mindustry.Vars.ui;

public class ContentSelectionDialog<T extends UnlockableContent> extends BaseDialog{
    private static final int emptyBuildConstructorHash = Blocks.air.buildType.hashCode();

    private final ContentType type;
    private ObjectSet<T> contentSet;
    private final Boolf<T> pred;
    public boolean isRevealedBlocks;
    private String searchText;
    private Category selectedCategory;
    private boolean terrainCategory;
    private Planet selectedPlanet;
    private Table selectedTable;
    private Table deselectedTable;
    private Seq<T> contentSelection;

    public ContentSelectionDialog(String title, ContentType type, Boolf<T> pred){
        super(title);
        this.type = type;
        this.pred = pred;
        isRevealedBlocks = false;
        terrainCategory = false;
        searchText = "";

        selectedTable = new Table();
        deselectedTable = new Table();

        addCloseButton();

        buttons.button("@rules.title.planet", Icon.planet, () -> {
            BaseDialog dialog = new BaseDialog("@rules.title.planet");
            dialog.cont.pane(table -> {
                int i = 1;
                table.button("@clear", Icon.none, Styles.togglet, () -> {
                            selectedPlanet = null;
                            dialog.hide();
                            rebuildTables();
                        }).marginLeft(14f).padBottom(5f).width(220f).height(55f).checked(selectedPlanet == null)
                        .update(b -> b.setChecked(selectedPlanet == null)).get().getChildren().get(1);
                for (Planet planet : content.planets()) {
                    if(!planet.accessible) continue;
                    table.button(planet.localizedName, Icon.icons.get(planet.icon), Styles.togglet, () -> {
                        if(selectedPlanet == planet) selectedPlanet = null;
                        else selectedPlanet = planet;
                        dialog.hide();
                        rebuildTables();
                    }).marginLeft(14f).padBottom(5f).width(220f).height(55f).checked(selectedPlanet == planet)
                    .update(b -> b.setChecked(selectedPlanet == planet)).get().getChildren().get(1).setColor(planet.iconColor);
                    i += 1;
                    if (i % 3 == 0) {
                        table.row();
                    }
                }
            });

            dialog.addCloseButton();
            dialog.show();
        });

        shown(this::build);
    }

    public void show(ObjectSet<T> contentSet){
        this.contentSet = contentSet;
        show();
    }

    public void build(){
        cont.clear();

        var cell = cont.table(table -> {
            table.table(table2 -> {
                table2.image(Icon.zoom).padRight(5f);
                table2.label(() -> "@search");
                var field = table2.field(searchText, value -> {searchText = value; rebuildTables();}).get();
                table2.button(Icon.none, () -> {
                    searchText = "";
                    field.setText("");
                    rebuildTables();
                }).padLeft(20f);
                table2.label(() -> selectedPlanet != null ? Core.bundle.get("rules.title.planet") + ": " + selectedPlanet.localizedName : "").padLeft(10f);
            });
            if(type == ContentType.block){
                table.row();
                table.table(table2 -> {
                    table2.marginTop(8f);
                    table2.defaults().marginRight(4f);
                    for (Category category : Category.values()){
                        table2.button(ui.getIcon(category.name()), Styles.squareTogglei, () -> {
                            if(selectedCategory == category){
                                selectedCategory = null;
                            }else{
                                selectedCategory = category;
                            }
                            terrainCategory = false;
                            rebuildTables();
                        }).size(45f).update(i -> i.setChecked(selectedCategory == category)).padLeft(4f);
                    }
                    table2.add("").padRight(4f);
                    if (isRevealedBlocks){
                        table2.button(ui.getIcon("terrain"), Styles.squareTogglei, () -> {
                            terrainCategory = !terrainCategory;
                            selectedCategory = null;
                            rebuildTables();
                        }).size(45f).update(i -> i.setChecked(terrainCategory)).padLeft(8f);
                    }
                }).center();
            }
        });
        cont.row();
        if(!Core.graphics.isPortrait()) cell.colspan(2);

        contentSelection = content.<T>getBy(type).select(pred);
        if(!searchText.isEmpty()) contentSelection.removeAll(content -> !content.localizedName.toLowerCase().contains(searchText.toLowerCase()));

        cont.table(table -> {
            if(isRevealedBlocks){
                table.add("@revealed_content").color(Pal.accent).padBottom(-1).top().row();
                table.image().color(Pal.accent).height(3f).padBottom(5f).fillX().expandX().top().row();
            }else{
                table.add("@banned_content").color(Color.valueOf("f25555")).padBottom(-1).top().row();
                table.image().color(Color.valueOf("f25555")).height(3f).padBottom(5f).fillX().expandX().top().row();
            }
            table.pane(table2 -> selectedTable = table2).fill().expand().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.addAll(contentSelection);
                rebuildTables();
            }).disabled(button -> contentSet.toSeq().containsAll(contentSelection)).padTop(10f).bottom().fillX();
        }).fill().expandY().uniform();

        if(Core.graphics.isPortrait()) cont.row();

        var cell2 = cont.table(table -> {
            if(isRevealedBlocks){
                table.add("@unrevealed_content").color(Color.valueOf("f25555")).padBottom(-1).top().row();
                table.image().color(Color.valueOf("f25555")).height(3f).padBottom(5f).fillX().top().row();
            }else{
                table.add("@unbanned_content").color(Pal.accent).padBottom(-1).top().row();
                table.image().color(Pal.accent).height(3f).padBottom(5f).fillX().top().row();
            }
            table.pane(table2 -> deselectedTable = table2).fill().expand().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.removeAll(contentSelection);
                rebuildTables();
            }).disabled(button -> {
                Seq<T> array = content.getBy(type);
                array = array.copy();
                array.removeAll(contentSet.toSeq());
                return array.containsAll(contentSelection);
            }).padTop(10f).bottom().fillX();
        }).fill().expandY().uniform();
        if(Core.graphics.isPortrait()){
            cell2.padTop(10f);
        }else{
            cell2.padLeft(10f);
        }

        rebuildTables();
    }

    private boolean belongsToPlanet(T content, Planet planet){
        if(content instanceof Block block){
            if(block.buildType.hashCode() == emptyBuildConstructorHash) return false;
            if(!block.supportsEnv(planet.defaultEnv)) return false;

            return block.isVisibleOn(planet);
        }else if(content instanceof UnitType unit){
            if(!unit.supportsEnv(planet.defaultEnv)) return false;

            for(var stack : unit.getTotalRequirements()){
                if(planet.hiddenItems.contains(stack.item)){
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private void rebuildTables(){
        contentSelection.clear();
        contentSelection = content.getBy(type);
        contentSelection = contentSelection.select(pred);

        if(!searchText.isEmpty()) contentSelection.removeAll(content -> !content.localizedName.toLowerCase().contains(searchText.toLowerCase()));
        if(selectedPlanet != null){
            contentSelection.removeAll(content -> !belongsToPlanet(content, selectedPlanet));
        }
        if(type == ContentType.block){
            contentSelection.removeAll(content -> {
                if(terrainCategory) return ((Block)content).buildVisibility != BuildVisibility.hidden;
                return selectedCategory != null && ((Block)content).category != selectedCategory;
            });
        }

        rebuildTable(selectedTable, true);
        rebuildTable(deselectedTable, false);
    }

    private void rebuildTable(Table table, boolean isSelected){
        table.clear();

        int buttonSize = settings.getInt("editor_content_buttons_size");
        int cols = settings.getInt("editor_better_content_dialogs_columns");
        if((isSelected && contentSet.isEmpty()) || (!isSelected && contentSet.size == content.<T>getBy(type).count(pred))){
            table.add("@empty").width(cols * buttonSize).padBottom(5f).get().setAlignment(Align.center);
        }else{
            Seq<T> array;
            if(!isSelected){
                array = content.getBy(type);
                array = array.copy();
                array.removeAll(contentSet.toSeq());
            }else{
                array = contentSet.toSeq();
            }
            array.sort();
            array.removeAll(content -> !contentSelection.contains(content));

            if(array.isEmpty()){
                table.add("@empty").width(cols * buttonSize).padBottom(5f).get().setAlignment(Align.center);
                return;
            }
            int i = 0;
            boolean requiresPad = true;

            for(T content : array){
                TextureRegion region = content.uiIcon;

                ImageButton button = new ImageButton(Tex.whiteui, Styles.squarei);
                button.getStyle().imageUp = new TextureRegionDrawable(region);
                button.resizeImage(8 * 4f);
                if(isSelected) button.clicked(() -> {
                    contentSet.remove(content);
                    rebuildTables();
                });
                else button.clicked(() -> {
                    contentSet.add(content);
                    rebuildTables();
                });
                table.add(button).size(buttonSize).tooltip(content.localizedName);

                if(++i % cols == 0){
                    table.row();
                    requiresPad = false;
                }
            }
            if(requiresPad){
                table.add("").padRight(buttonSize * (cols - i));
            }
        }
    }
}
