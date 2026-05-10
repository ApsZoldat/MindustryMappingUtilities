package mu.ui.dialogs;

import arc.Core;
import arc.graphics.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mu.ui.*;
import mu.ui.data.*;

import static mu.MUVars.*;

public class UIExplorerDialog extends BaseDialog{
    public ElementData currentElement;

    public Table pathTable;
    public Seq<ElementData> pathStack = new Seq<>();

    public UIExplorerDialog(){
        super("temp");

        currentElement = null;
        pathTable = new Table();

        addCloseButton();

        shown(this::build);

        hidden(() -> {
            windows.clear();
            for(WindowData data : windowsData){
                windows.addChild(new Window(data));
            }
        });
    }

    public void build(){
        cont.clear();

        if(currentElement == null){
            buildWindowSelection();
        }else{
            buildPath();
            cont.add(pathTable).growX().left().row();
            cont.image().color(Pal.accent).height(3f).padTop(6f).padBottom(20).fillX().row();
            cont.pane(p -> p.add(currentElement.explorerSettings(this))).grow();
        }
    }

    public void buildWindowSelection(){
        cont.pane(p -> {
            for(WindowData window : windowsData){
                String name = window.name;

                p.button(name, Styles.flatTogglet, () -> {
                    currentElement = window;
                    pathStack.clear();
                    pathStack.add(window);
                    build();
                }).width(300f).minHeight(50f).row();
            }
        });
    }

    public void buildPath(){
        pathTable.clear();
        pathTable.button(Icon.left, () -> {
            currentElement = null;
            pathStack.clear();
            build();
        }).size(40f).padRight(8f);

        pathTable.pane(p -> {
            for(ElementData data : pathStack){
                if(data instanceof WindowData w){
                    p.button(w.name, () -> {
                        currentElement = data;
                        cutPathTo(data);
                        build();
                    }).left().height(40f).get().getLabel().setWrap(false);
                }else{
                    p.button(data.getClass().getSimpleName().replace("Data", ""), () -> {
                        currentElement = data;
                        cutPathTo(data);
                        build();
                    }).left().height(50f).get().getLabel().setWrap(false);
                }  // TODO: actually make this at the left side
            }
        }).scrollX(true).growX().left();
    }

    public void cutPathTo(ElementData element){
        Seq<ElementData> newStack = new Seq<>();

        for(ElementData data : pathStack){
            newStack.add(data);
            if(data == element) break;
        }
        pathStack = newStack;
    }

    public void numberi(Table table, String text, Intc cons, Intp prov, int min, int max){
        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
                .get().setColor(Color.white);
            t.field((prov.get()) + "", s -> cons.get(Strings.parseInt(s)))
                .padRight(100f)
                .valid(f -> Strings.parseInt(f) >= min && Strings.parseInt(f) <= max).width(120f).left();
        }).padTop(0f);
        table.row();
    }

    public void number(Table table, String text, Floatc cons, Floatp prov, float min, float max){
        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
            .get().setColor(Color.white);
            t.field(prov.get() + "", s -> cons.get(Strings.parseFloat(s)))
            .padRight(50f)
            .valid(f -> Strings.canParsePositiveFloat(f) && Strings.parseFloat(f) >= min && Strings.parseFloat(f) <= max).width(120f).left();
        }).padTop(0f);
        table.row();
    }

    public void check(Table table, String text, Boolc cons, Boolp prov){
        table.check(text, cons).checked(prov.get()).get().left().marginTop(8f);
        table.row();
    }
}