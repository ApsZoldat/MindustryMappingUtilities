package mu.ui.data;

import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;
import mu.utils.*;
import mu.utils.MUAnnotations.*;

import static mindustry.Vars.*;

public class ButtonData extends UIObjectData implements ElementData{
    public boolean isChecked = false;
    public boolean isDisabled = false;

    public @NoCopy String styleName = "";

    public @NoCopy String script = "";

    public Button build(){
        Button button = new Button(Styles.defaultb);
        MUReflect.copyFields(this, button);
        button.clicked(() -> mods.getScripts().runConsole(script));
        return button;
    }
    
    public Button buildPreview(UIExplorerDialog dialog){
        Button button = build();
        MUReflect.copyFields(this, button);
        button.touchable = Touchable.disabled;
        return button;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.check(table, "IsChecked", "isChecked");
        dialog.check(table, "IsDisabled", "isDisabled");

        table.add("JS Script").padTop(10f).padBottom(2f).center().row();
        table.field(script, v -> dialog.currentGroup.each(b -> ((ButtonData) b).script = v)).size(400f, 300f).padBottom(10f).row();

        return table;
    }

    public void replaceChild(UIObjectData oldData, UIObjectData newData){
        return;
    }
}