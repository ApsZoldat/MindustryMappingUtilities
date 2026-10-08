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

public class ButtonData extends UIObjectData{
    public boolean isChecked = false;
    public boolean isDisabled = false;

    public @NoCopy String styleName = "";

    public @NoCopy String clickedScript = null;

    public Button build(){
        Button button = new Button(Styles.defaultb);
        this.object = button;

        MUReflect.copyFields(this, button);

        // TODO: all kinds of listeners
        button.clicked(() -> runScript(clickedScript));
        runScript(buildScript);
        return button;
    }
    
    public Button buildPreview(UIExplorerDialog dialog){
        Button button = build();
        button.touchable = Touchable.disabled;
        return button;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();
        table.defaults().fillX().left();

        dialog.check(table, "IsChecked", "isChecked");
        dialog.check(table, "IsDisabled", "isDisabled");

        table.add("JS Script (On Button Click)").padTop(10f).padBottom(2f).center().row();
        table.area(clickedScript, v -> dialog.currentGroup.each(b -> ((ButtonData) b).clickedScript = v)).size(400f, 300f).padBottom(10f).maxTextLength(Integer.MAX_VALUE).row();

        table.add(super.explorerSettings(dialog));
        return table;
    }
}