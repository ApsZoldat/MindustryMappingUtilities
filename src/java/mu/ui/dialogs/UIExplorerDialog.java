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
import mindustry.io.*;
import mindustry.Vars;
import mu.ui.*;
import mu.ui.data.*;

import static mu.EditorVars.*;

public class UIExplorerDialog extends BaseDialog{
    public UIObjectData currentData = null;
    public Seq<UIObjectData> currentGroup = new Seq<>();

    public Seq<CellData> selectedCells = new Seq<>();

    public Table pathTable = new Table();
    public Seq<UIObjectData> pathStack = new Seq<>();

    public UIExplorerDialog(){
        super("temp");

        addCloseButton();

        buttons.button("@edit", Icon.edit, this::editDialog);

        shown(this::build);

        hidden(() -> {
            windows.clear();
            for(WindowData data : windowsData){
                windows.addChild(new Window(data));
            }
        });
    }

    public void selectGroup(Class<?> cls){
        currentGroup.clear();

        if(selectedCells.isEmpty() || cls == null){
            currentGroup.add(currentData);
        }else if(cls == CellData.class){
            currentGroup.addAll(selectedCells);
        }else{
            for(CellData cell : selectedCells){
                ElementData data = cell.element;
                if(cls.isInstance(data)){
                    currentGroup.add((UIObjectData) data);
                }
            }
        }
    
        currentData = currentGroup.get(0);
        pathStack.add(currentData);
    }

    public void selectData(UIObjectData data){
        currentData = data;
        currentGroup.clear();
        currentGroup.add(data);
        cutPathTo(data);
        if(data != null) pathStack.add(data);
    }

    public void cutPathTo(UIObjectData data){
        if(data == null){
            pathStack.clear();
            return;
        }

        Seq<UIObjectData> newStack = new Seq<>();

        for(UIObjectData d : pathStack){
            if(d == data) break;
            newStack.add(d);
        }
        pathStack = newStack;
    }

    public void build(){
        cont.clear();

        if(currentData == null){
            buildWindowSelection();
        }else{
            buildPath();
            cont.add(pathTable).growX().left().row();
            cont.image().color(Pal.accent).height(3f).padTop(6f).padBottom(20).fillX().row();

            cont.pane(p -> p.add(currentData.explorerSettings(this))).grow();
        }
    }

    public void buildWindowSelection(){
        cont.pane(p -> {
            for(WindowData window : windowsData){
                String name = window.name;

                p.button(name, Styles.flatTogglet, () -> {
                    selectData(window);
                    build();
                }).width(300f).minHeight(50f).row();
            }
        });
        cont.row();
        cont.button("@add", Icon.add, () -> {
            WindowData data = new WindowData();
            selectData(data);
            windowsData.add(data);
            build();
        }).width(320f).minHeight(50f).padTop(40f);
    }

    public void buildPath(){
        pathTable.clear();
        pathTable.button(Icon.left, () -> {
            selectData(null);
            build();
        }).size(40f).padRight(8f);

        pathTable.pane(p -> {
            for(UIObjectData data : pathStack){
                if(data instanceof WindowData w){
                    p.button(w.name, () -> {
                        selectData(data);
                        build();
                    }).left().height(40f).get().getLabel().setWrap(false);
                }else{  // TODO: remove this shit maybe
                    String text = data.getClass().getSimpleName().replace("Data", "");
                    if(data == pathStack.get(pathStack.size - 1) && currentGroup.size > 1) text += " x" + currentGroup.size;
                    p.button(text, () -> {
                        selectData(data);
                        build();
                    }).left().height(40f).get().getLabel().setWrap(false);
                }
            }
        }).scrollX(true).growX().left();
    }

    public void numberi(Table table, String text, String property, int min, int max, int step){
        Intp prov = () -> Reflect.get(currentData, property);
        Intc cons = f -> currentGroup.each(d -> Reflect.set(d, property, f));

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
        Floatp prov = () -> Reflect.get(currentData, property);
        Floatc cons = f -> currentGroup.each(d -> Reflect.set(d, property, f));

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
        Boolp prov = () -> Reflect.get(currentData, property);
        Boolc cons = f -> currentGroup.each(d -> Reflect.set(d, property, f));

        table.check(text, b -> cons.get(b)).checked(prov.get()).get().left().marginTop(8f);
        table.row();
    }

    public void alignment(Table table, String property){
        Intp prov = () -> Reflect.get(currentData, property);
        Intc cons = f -> currentGroup.each(d -> Reflect.set(d, property, f));

        table.button("", () -> cons.get(Align.left & Align.top)).size(50f);
        table.button(Icon.up, () -> cons.get(Align.top)).size(50f);
        table.button("", () -> cons.get(Align.right & Align.top)).size(50f);
        table.row();
        table.button(Icon.left, () -> cons.get(Align.left)).size(50f);
            
        table.button("", () -> cons.get(Align.center)).size(50f);  // TODO: find icon for ts
        table.button(Icon.right, () -> cons.get(Align.right)).size(50f);
        table.row();
        table.button("", () -> cons.get(Align.left & Align.bottom)).size(50f);
        table.button(Icon.down, () -> cons.get(Align.bottom)).size(50f);
        table.button("", () -> cons.get(Align.right & Align.bottom)).size(50f);
    }

    public void editDialog(){
        BaseDialog dialog = new BaseDialog("@edit");

        dialog.cont.pane(p -> {
            p.margin(10f);
            p.table(Tex.button, t -> {
                t.defaults().size(450f, 60f).left();

                t.button("@waves.copy", Icon.copy, Styles.flatt, () -> {
                    Core.app.setClipboardText(JsonIO.write(currentData));
                    Vars.ui.showInfoFade("@copied");
                    dialog.hide();
                }).disabled(currentData == null).marginLeft(12f).row();
                t.button("@waves.load", Icon.download, Styles.flatt, () -> {
                    try{
                        replaceCurrentData(() -> JsonIO.read(currentData.getClass(), Core.app.getClipboardText()));
                    }catch (Exception err){
                        Log.err(err);
                        Vars.ui.showErrorMessage("temp");
                    }
                    build();
                    dialog.hide();
                }).disabled(Core.app.getClipboardText() == null).marginLeft(12f).row();
            });
        });

        dialog.addCloseButton();
        dialog.show();
    }

    public void replaceCurrentData(Prov<UIObjectData> prov){
        if(currentData instanceof WindowData curdata){
            UIObjectData newData = prov.get();
            if(newData instanceof WindowData w){
                if(!windowsData.replace(curdata, w)) throw new RuntimeException("Invalid data importing target.");
            }else{
                throw new RuntimeException("Invalid data format. Expected WindowData.");
            }
            currentData = newData;
        }else{
            currentGroup.each(d -> {
                UIObjectData newData = prov.get();
                currentGroup.replace(d, newData);
                pathStack.get(pathStack.size - 2).replaceChild(d, newData);
            });
            currentData = currentGroup.get(0);
        }
    }

    public void layoutDialog(){
        BaseDialog dialog = new BaseDialog("temp");

        if(currentData instanceof ElementData elem){
            dialog.cont.pane(p -> {
                p.add(elem.buildPreview(this));
            });
        }else{
            dialog.cont.add("Oops, perhaps this is not even an element!");
        }

        dialog.addCloseButton();
        
        dialog.buttons.button("Edit", Icon.edit, () -> {
            selectGroupDialog(dialog);
        }).disabled(b -> selectedCells.isEmpty());
        dialog.buttons.button("Add", Icon.add, () -> {
            addElementDialog(dialog);
        }).disabled(b -> !(currentData instanceof TableData));


        dialog.hidden(() -> selectedCells.clear());

        dialog.show();
    }

    public void addElementDialog(BaseDialog prev){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.addCloseButton();

        // TODO: unholy fucking shit.
        dialog.cont.button("Button", () -> {
            CellData cell = new CellData(new ButtonData());
            cell.minWidth = cell.maxWidth = cell.minHeight = cell.maxHeight = 50f;
            ((TableData) currentData).cells.add(cell);
            dialog.hide();
            prev.hide();
            layoutDialog();
        }).padBottom(5f).width(300f).minHeight(50f).row();
        dialog.cont.button("Table", () -> {
            CellData cell = new CellData(new TableData());
            cell.minWidth = cell.minHeight = 50f;
            ((TableData) currentData).cells.add(cell);
            dialog.hide();
            prev.hide();
            layoutDialog();
        }).padBottom(5f).width(300f).minHeight(50f).row();
        dialog.cont.button("Row", () -> {
            Seq<CellData> cells = ((TableData) currentData).cells;
            if(cells.isEmpty()) return;
            cells.get(cells.size - 1).endRow = true;
            dialog.hide();
            prev.hide();
            layoutDialog();
        }).padBottom(5f).width(300f).minHeight(50f).row();

        dialog.show();
    }

    public void selectGroupDialog(BaseDialog prev){
        BaseDialog dialog = new BaseDialog("temp");

        dialog.cont.pane(p -> {
            classButton(p, CellData.class, dialog);
            classButton(p, TableData.class, dialog);
            classButton(p, ButtonData.class, dialog);
        });

        dialog.addCloseButton();

        dialog.hidden(() -> prev.hide());

        dialog.show();
    }

    public void classButton(Table table, Class cls, BaseDialog dialog){
        table.button(cls.getSimpleName().replace("Data", ""), () -> {
            selectGroup(cls);
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
