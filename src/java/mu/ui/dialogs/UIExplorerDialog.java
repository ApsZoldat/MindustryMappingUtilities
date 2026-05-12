package mu.ui.dialogs;

import arc.Core;
import arc.graphics.*;
import arc.func.*;
import arc.math.*;
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
    public ElementData currentElement = null;

    public Table pathTable = new Table();;
    public Seq<ElementData> pathStack = new Seq<>();
    
    public TableLayoutDialog layoutDialog = new TableLayoutDialog();

    public UIExplorerDialog(){
        super("temp");

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
        cont.row();
        cont.button("@add", Icon.add, () -> {
            WindowData data = new WindowData();
            currentElement = data;
            pathStack.add(data);
            windowsData.add(data);
            build();
        }).width(320f).minHeight(50f).padTop(40f);
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
                    }).left().height(40f).get().getLabel().setWrap(false);
                }
            }
            p.add("").growX().left();
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

    public static void numberi(Table table, String text, Intc cons, Intp prov, int min, int max, int step){
        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
                .get().setColor(Color.white);
            t.field((prov.get()) + "", s -> cons.get(Strings.parseInt(s)))
                .padRight(100f)
                .valid(f -> Strings.parseInt(f) >= min && Strings.parseInt(f) <= max)
                .update(c -> {c.setText(prov.get() + "");})
                .width(120f).left().padRight(20f);
            t.button("+", () -> cons.get(Mathf.clamp(prov.get() + step, min, max))).size(40f).padRight(5f);
            t.button("-", () -> cons.get(Mathf.clamp(prov.get() - step, min, max))).size(40f);
        }).padTop(0f);
        table.row();
    }

    public static void number(Table table, String text, Floatc cons, Floatp prov, float min, float max, float step){
        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
            .get().setColor(Color.white);
            t.field(prov.get() + "", s -> cons.get(Strings.parseFloat(s)))
                .padRight(50f)
                .valid(f -> Strings.canParsePositiveFloat(f) && Strings.parseFloat(f) >= min && Strings.parseFloat(f) <= max)
                .update(c -> {c.setText(prov.get() + "");})
                .width(120f).left().padRight(20f);
            t.button("+", () -> cons.get(Mathf.clamp(prov.get() + step, min, max))).size(40f).padRight(5f);
            t.button("-", () -> cons.get(Mathf.clamp(prov.get() - step, min, max))).size(40f);
        }).padTop(0f);
        table.row();
    }

    public static void check(Table table, String text, Boolc cons, Boolp prov){
        table.check(text, cons).checked(prov.get()).get().left().marginTop(8f);
        table.row();
    }
    
    public static void alignment(Table table, Intc cons){
        table.button("", () -> cons.get(Align.left & Align.top)).size(50f);
        table.button(Icon.up, () -> cons.get(Align.top)).size(50f);
        table.button("", () -> cons.get(Align.right & Align.top)).size(50f);
        table.row();
        table.button(Icon.left, () -> cons.get(Align.top)).size(50f);
            
        table.button("", () -> cons.get(Align.center)).size(50f);  // TODO: find icon for ts
        table.button(Icon.right, () -> cons.get(Align.right)).size(50f);
        table.row();
        table.button("", () -> cons.get(Align.left & Align.bottom)).size(50f);
        table.button(Icon.down, () -> cons.get(Align.bottom)).size(50f);
        table.button("", () -> cons.get(Align.right & Align.bottom)).size(50f);
    }

    public void elementSelectionDialog(){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.cont.pane(p -> {
            int i = 0;

            p.add("Row " + (++i)).row();
            p.image().color(Color.white).height(3f).padTop(6f).padBottom(8f).left().fillX().row();
            // TODO: add <empty>

            for(CellData cell : ((TableData) currentElement).cells){
                ElementData elem = cell.element;

                // TODO: define style in elements themselves
                p.button(elem.getClass().getSimpleName().replace("Data", ""), () -> {
                    currentElement = elem;
                    pathStack.add(elem);
                    build();
                    dialog.hide();
                }).padTop(10f).width(300f).minHeight(50f).row();

                if(cell.endRow){
                    p.add("Row " + (++i)).padTop(10f).row();
                    p.image().color(Color.white).height(3f).padTop(6f).padBottom(8f).left().fillX().row();
                }
            }
        });
        dialog.cont.row();
        dialog.cont.button("@add", Icon.add, () -> elementAdditionDialog(dialog)).width(320f).minHeight(50f).padTop(40f);
        
        dialog.addCloseButton();
        dialog.show();
    }

    public void elementAdditionDialog(BaseDialog selectionDialog){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.cont.button("Button", () -> {
            CellData cell = new CellData(new ButtonData());
            cell.minWidth = cell.maxWidth = cell.minHeight = cell.maxHeight = 50f;
            ((TableData) currentElement).cells.add(cell);
            dialog.hide();
            selectionDialog.hide();
            elementSelectionDialog();
        }).padTop(10f).width(300f).minHeight(50f).row();

        dialog.addCloseButton();
        dialog.show();
    }
}
