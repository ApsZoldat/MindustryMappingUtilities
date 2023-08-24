package mu.ui;

import arc.scene.event.VisibilityListener;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.CustomRulesDialog;
import mu.reflect.ui.RulesDialog;

public class RulesSearchDialog extends CustomRulesDialog{
    private Table resultsTable;
    private String searchText = "";

    public RulesSearchDialog(){
        super();
        // Add clear listener to the beginning of listeners sequence
        this.getListeners().insert(0, new VisibilityListener(){
            @Override
            public boolean shown(){
                Table main = getMain();
                if(main != null) getMain().clear();
                return false;
            }
        });
        RulesDialog.change(this);
        shown(this::addSearchBar);
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
        main.getCells().each(cell -> {
            String labelText = RulesDialog.getLabelText(cell.get());
            if(labelText == null) return;
            if(hasWordsParts(labelText, text)) resultsTable.add(cell.get()).row();
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
