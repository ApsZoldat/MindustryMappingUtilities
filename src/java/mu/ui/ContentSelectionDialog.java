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
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.content;

public class ContentSelectionDialog<T extends UnlockableContent> extends BaseDialog{
    private final ContentType type;
    private final ObjectSet<T> contentSet;
    private final Boolf<T> pred;
    public boolean isRevealedBlocks;

    private Table selectedTable;
    private Table deselectedTable;

    public ContentSelectionDialog(String title, ContentType type, ObjectSet<T> contentSet, Boolf<T> pred){
        super(title);
        this.type = type;
        this.contentSet = contentSet;
        this.pred = pred;

        selectedTable = new Table();
        deselectedTable = new Table();

        addCloseButton();

        shown(this::build);
    }

    public void build(){
        cont.clear();
        cont.table(table -> {
            if(isRevealedBlocks){
                table.add("@revealed_content").color(Pal.accent).padBottom(-1);
            }else{
                table.add("@banned_content").color(Color.valueOf("f25555")).padBottom(-1);
            }
            table.row();
            table.image().color(isRevealedBlocks ? Pal.accent : Color.valueOf("f25555")).height(3f).padBottom(20).fillX();
            table.row();
            table.pane(table2 -> selectedTable = table2).fillX().fillY().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.addAll(content.<T>getBy(type).select(pred));
                rebuildTables();
            }).bottom().fillX();
        }).top();
        if(Core.graphics.isPortrait()) cont.row();
        cont.table(table -> {
            if(isRevealedBlocks){
                table.add("@unrevealed_content").color(Color.valueOf("f25555")).padBottom(-1);
            }else{
                table.add("@unbanned_content").color(Pal.accent).padBottom(-1);
            }
            table.row();
            table.image().color(isRevealedBlocks ? Color.valueOf("f25555") : Pal.accent).height(3f).padBottom(20).fillX();
            table.row();
            table.pane(table2 -> deselectedTable = table2).fillX().fillY().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.removeAll(content.<T>getBy(type).select(pred));
                rebuildTables();
            }).bottom().fillX();
        }).top();

        rebuildTables();
    }

    private void rebuildTables(){
        rebuildTable(selectedTable, true);
        rebuildTable(deselectedTable, false);
    }

    private void rebuildTable(Table table, boolean isSelected){
        table.clear();
        table.margin(10f);

        if((isSelected && contentSet.isEmpty()) || (!isSelected && contentSet.size == content.<T>getBy(type).count((pred)))){
            table.add("@empty");
        }else{
            Seq<T> array;
            if(!isSelected){
                array = content.getBy(type);
                array.removeAll(contentSet.toSeq());
            }else{
                array = contentSet.toSeq();
            }
            array.sort();

            int i = 0;

            for(T content : array){
                if(!pred.get(content)) continue;
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
                table.add(button).size(50f).tooltip(content.localizedName);

                if(++i % 8 == 0){
                    table.row();
                }
            }
        }
    }
}
