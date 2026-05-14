package mu.ui.data;

import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;
import mu.ui.data.annotations.*;

public class ButtonData extends TableData{
    public boolean isChecked = false;
    public boolean isDisabled = false;

    public @NoCopy String styleName = "";

    public Button build(){
        Button button = new Button(Styles.defaultb);
        copyFields(button);
        return button;
    }
    
    public Button buildPreview(UIExplorerDialog dialog){
        Button button = build();
        copyFields(button);
        button.touchable = Touchable.disabled;
        return button;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        Table table = new Table();

        dialog.check(table, "IsChecked", "isChecked");
        dialog.check(table, "IsDisabled", "isDisabled");

        return table;
    }
}