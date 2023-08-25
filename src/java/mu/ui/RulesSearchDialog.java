package mu.ui;

import arc.scene.ui.Image;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;
import mu.modifying.ui.RulesDialog;

public class RulesSearchDialog extends CustomRulesDialog{
    private Table resultsTable;
    private String searchText = "";

    public RulesSearchDialog(){
        super();
        RulesDialog.modify(this);
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

    void title(String text){
        Table main = getMain();
        main.add(text).color(Pal.accent).padTop(20).padRight(100f).padBottom(-3);
        main.row();
        main.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20);
        main.row();
    }

    private void buildMain(){
        Reflect.invoke(CustomRulesDialog.class, this, "setup", null);
        RulesDialog.setup(this);
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
            String labelText = RulesDialog.getLabelText(cell.get());
            if(labelText == null) return false;
            return hasWordsParts(labelText, text);
        });

        main.getCells().each(cell -> {
            // Adding team rules buttons with collapsers
            if(cell.get() instanceof Collapser){
                if(includeCollapsers){
                    Table newTable = new Table();
                    newTable.left().defaults().fillX().left().pad(5);
                    ((Table)Reflect.get(Collapser.class, cell.get(), "table")).getCells().each(cell2 -> {
                        String labelText = RulesDialog.getLabelText(cell2.get());
                        if(labelText == null) return;
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
                String labelText = RulesDialog.getLabelText(cell.get());
                if(labelText == null) return;
                if(hasWordsParts(labelText, text)) resultsTable.add(cell.get()).row();
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
                if(textWord.contains(word)) {
                    found = true;
                    break;
                }
            }
            if(!found) return false;
        }

        return true;
    }
}
