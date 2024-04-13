package mu.legacy.ui;

import arc.scene.Element;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;
import arc.util.Reflect;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;
import mu.legacy.modifying.ui.LegacyRulesDialog;

import static mu.legacy.modifying.ui.LegacyRulesDialog.*;

public class LegacyRulesSearchDialog extends CustomRulesDialog{
    private Table resultsTable;
    private String searchText = "";

    public LegacyRulesSearchDialog(){
        super();
        LegacyRulesDialog.modify(this);
        shown(() -> {
            if(!searchText.isEmpty()) search(searchText);
            addSearchBar();
        });
    }

    private void addSearchBar(){
        cont.clear();
        cont.table(table -> {
            table.label(() -> "@search");
            TextField field = table.field(searchText, value -> {}).get();
            table.button(Icon.zoom, () -> search(field.getText())).padLeft(5f);
        }).row();
        if(resultsTable == null){
            resultsTable = new Table();
            resultsTable.add(getMain());
        }
        cont.pane(resultsTable);
    }

    public Table getMain(){
        return Reflect.get(CustomRulesDialog.class, this, "main");
    }

    // Used by Reflect.invoke
    @SuppressWarnings("unused")
    void title(String text){
        Table main = getMain();
        main.add(text).color(Pal.accent).padTop(20).padRight(100f).padBottom(-3);
        main.row();
        main.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20);
        main.row();
    }

    private void buildMain(){
        Reflect.invoke(CustomRulesDialog.class, this, "setup", null);
        LegacyRulesDialog.setup(this);
    }

    private void search(String text){
        searchText = text;
        resultsTable = null;
        if(text.isEmpty()){
            buildMain();
            addSearchBar();
            return;
        }

        resultsTable = new Table();
        resultsTable.left().defaults().fillX().left().pad(5);
        buildMain();
        Table main = getMain();

        Collapser collapser = (Collapser)main.getCells().find(cell -> cell.get() instanceof Collapser).get();
        boolean includeCollapsers = ((Table)Reflect.get(collapser, "table")).getCells().contains(cell -> {
            String labelText = LegacyRulesDialog.getLabelText(cell.get());
            if(labelText == null) return false;
            return hasWordsParts(labelText, text);
        });

        main.getCells().each(cell -> {
            var elem = cell.get();

            // Adding team rules buttons with collapsers
            if(elem instanceof Collapser){
                if(includeCollapsers){
                    Table newTable = new Table();
                    newTable.left().defaults().fillX().left().pad(5);
                    ((Table)Reflect.get(Collapser.class, elem, "table")).getCells().each(cell2 -> {
                        String labelText = LegacyRulesDialog.getLabelText(cell2.get());
                        if(!isRuleKey(getBundleKey(labelText))) return;
                        if(hasWordsParts(labelText, text)) newTable.add(cell2.get()).row();
                    });

                    Collapser newCollapser = new Collapser(newTable, true);
                    TextButton prevButton = (TextButton)(main.getCells().get(main.getCells().indexOf(cell) - 1).get());

                    resultsTable.button(prevButton.getText().toString(), Icon.downOpen, Styles.togglet, () -> newCollapser.toggle(false)).marginLeft(14f).width(260f).height(55f).update(t -> {
                        ((Image)t.getChildren().get(1)).setDrawable(!newCollapser.isCollapsed() ? Icon.upOpen : Icon.downOpen);
                        t.setChecked(!newCollapser.isCollapsed());
                    }).row();
                    resultsTable.add(newCollapser).row();
                }
            }else{
                String labelText = LegacyRulesDialog.getLabelText(elem);
                if(!isRuleKey(getBundleKey(labelText))) return;
                if(hasWordsParts(labelText, text)){
                    Cell<Element> elemCell = resultsTable.add(elem);
                    if(elem instanceof Button && !(elem instanceof CheckBox)) elemCell.width(300f);
                    resultsTable.row();
                }
            }
        });
        addSearchBar();
    }

    // Used for "light" rules search
    private boolean hasWordsParts(String text, String wordsString){
        text = text.toLowerCase();
        wordsString = wordsString.toLowerCase();

        String[] textWords = text.split(" ");
        String[] words = wordsString.split(" ");


        for(String word : words){
            boolean found = false;
            for(String textWord : textWords){
                if(textWord.contains(word)){
                    found = true;
                    break;
                }
            }
            if(!found) return false;
        }

        return true;
    }
}
