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

    // Multiple element/cell editing
    public Seq<CellData> selectedCells = new Seq<>();
    public Class groupClass = null;

    public Table pathTable = new Table();
    public Seq<ElementData> pathStack = new Seq<>();

    public UIExplorerDialog(){
        super("temp");

        addCloseButton();
        
        buttons.button("Preview", Icon.eye, () -> previewDialog()).disabled(b -> currentElement == null);

        shown(this::build);

        hidden(() -> {
            windows.clear();
            for(WindowData data : windowsData){
                windows.addChild(new Window(data));
            }
        });
    }

    public Seq<Object> getCurrentGroup(){
        Seq<Object> group = new Seq<>();
        
        if(selectedCells.isEmpty() || groupClass == null){
            group.add(currentElement);
            return group;
        }

        if(groupClass == CellData.class){
            group.addAll(selectedCells);
            return group;
        }

        for(CellData cell : selectedCells){
            ElementData element = cell.element;
            if(groupClass.isInstance(element)){
                group.add(element);
            }
        }
        
        return group;
    }

    public void build(){
        cont.clear();

        if(currentElement == null){
            buildWindowSelection();
        }else{
            buildPath();
            cont.add(pathTable).growX().left().row();
            cont.image().color(Pal.accent).height(3f).padTop(6f).padBottom(20).fillX().row();
            if(!selectedCells.isEmpty() && groupClass != null){
                if(groupClass == CellData.class){
                    cont.pane(p -> p.add(CellData.explorerSettings(this))).grow();
                }else{
                    cont.pane(p -> p.add(((ElementData) getCurrentGroup().get(0)).explorerSettings(this))).grow();
                }
            }else{
                cont.pane(p -> p.add(currentElement.explorerSettings(this))).grow();
            }
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
            groupClass = null;
            selectedCells.clear();
            pathStack.clear();
            build();  // TODO: try using cutPathTo(null)
        }).size(40f).padRight(8f);

        pathTable.pane(p -> {
            for(ElementData data : pathStack){
                if(data instanceof WindowData w){
                    p.button(w.name, () -> {
                        cutPathTo(data);
                        build();
                    }).left().height(40f).get().getLabel().setWrap(false);
                }else{
                    p.button(data.getClass().getSimpleName().replace("Data", ""), () -> {
                        cutPathTo(data);
                        build();
                    }).left().height(40f).get().getLabel().setWrap(false);
                }
            }
            if(!selectedCells.isEmpty() && groupClass != null){
                p.button(groupClass.getSimpleName().replace("Data", "") + " x" + getCurrentGroup().size, () -> {});
            }
            p.add("").growX().left();
        }).scrollX(true).growX().left();
    }

    public void cutPathTo(ElementData element){
        currentElement = element;
        groupClass = null;
        selectedCells.clear();
        Seq<ElementData> newStack = new Seq<>();

        for(ElementData data : pathStack){
            newStack.add(data);
            if(data == element) break;
        }
        pathStack = newStack;
    }

    public void numberi(Table table, String text, String property, int min, int max, int step){
        Seq<Object> group = getCurrentGroup();
        Intp prov = () -> Reflect.get(group.get(0), property);
        Intc cons = f -> group.each(e -> Reflect.set(e, property, f));

        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
                .get().setColor(Color.white);
            t.field((prov.get()) + "", s -> cons.get(Strings.parseInt(s)))
                .padRight(100f)
                .valid(f -> Strings.parseInt(f) >= min && Strings.parseInt(f) <= max)
                .update(c -> {c.setText(prov.get() + "");})
                .width(120f).left().padRight(20f);
            t.button("-", () -> cons.get(Mathf.clamp(prov.get() - step, min, max))).size(40f).padRight(5f);
            t.button("+", () -> cons.get(Mathf.clamp(prov.get() + step, min, max))).size(40f);
        }).padTop(0f);
        table.row();
    }

    public void number(Table table, String text, String property, float min, float max, float step){
        Seq<Object> group = getCurrentGroup();
        Floatp prov = () -> Reflect.get(group.get(0), property);
        Floatc cons = f -> group.each(e -> Reflect.set(e, property, f));

        table.table(t -> {
            t.left();
            t.add(text).left().padRight(5f)
            .get().setColor(Color.white);
            t.field(prov.get() + "", s -> cons.get(Strings.parseFloat(s)))
                .padRight(50f)
                .valid(f -> Strings.canParsePositiveFloat(f) && Strings.parseFloat(f) >= min && Strings.parseFloat(f) <= max)
                .update(c -> {c.setText(prov.get() + "");})
                .width(120f).left().padRight(20f);
            t.button("-", () -> cons.get(Mathf.clamp(prov.get() - step, min, max))).size(40f).padRight(5f);
            t.button("+", () -> cons.get(Mathf.clamp(prov.get() + step, min, max))).size(40f);
        }).padTop(0f);
        table.row();
    }

    public void check(Table table, String text, String property){
        Seq<Object> group = getCurrentGroup();
        Boolp prov = () -> Reflect.get(group.get(0), property);
        Boolc cons = f -> group.each(e -> Reflect.set(e, property, f));


        table.check(text, b -> cons.get(b)).checked(prov.get()).get().left().marginTop(8f);
        table.row();
    }
    
    public void alignment(Table table, String property){
        Seq<Object> group = getCurrentGroup();
        Intc cons = f -> group.each(e -> Reflect.set(e, property, f));

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

    public void addElementDialog(BaseDialog prev){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.addCloseButton();
        
        dialog.cont.button("Button", () -> {
            CellData cell = new CellData(new ButtonData());
            cell.minWidth = cell.maxWidth = cell.minHeight = cell.maxHeight = 50f;
            ((TableData) currentElement).cells.add(cell);
            dialog.hide();
            prev.hide();
            previewDialog();
        }).padTop(10f).width(300f).minHeight(50f).row();

        dialog.show();
    }

    public void previewDialog(){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.cont.pane(p -> {
            p.add(currentElement.buildPreview(this));
        });

        dialog.addCloseButton();
        
        dialog.buttons.button("Edit", Icon.edit, () -> {
            selectGroupClass(dialog);
        }).disabled(b -> selectedCells.isEmpty());
        dialog.buttons.button("Add", Icon.add, () -> {
            addElementDialog(dialog);
        });


        dialog.hidden(() -> {
            if(groupClass == null) selectedCells.clear();
        });

        dialog.show();
    }

    public void selectGroupClass(BaseDialog prev){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.cont.pane(p -> {
            classButton(p, CellData.class, dialog);
            classButton(p, TableData.class, dialog);
            classButton(p, ButtonData.class, dialog);
        });

        dialog.addCloseButton();

        dialog.hidden(() -> {
            if(groupClass != null) prev.hide();
        });

        dialog.show();
    }

    public void classButton(Table table, Class cls, BaseDialog dialog){
        table.button(cls.getSimpleName().replace("Data", ""), () -> {
            groupClass = cls;
            build();
            dialog.hide();
        }).padBottom(10f).width(300f).minHeight(50f).disabled(b -> {
            if(cls == CellData.class) return false;
            
            for(CellData cell : selectedCells){
                if(cls.isInstance(cell.element)) return false;
            }
            return true;
        }).row();
    }
}
