package mu.ui.data;

import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;

public class ButtonData extends ElementData{
    public Button build(){
        return new Button(Styles.defaultb);
    }
    
    public Button buildPreview(UIExplorerDialog dialog){
        Button button = build();
        button.touchable = Touchable.disabled;
        return button;
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        return new Table();
    }
}