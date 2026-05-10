package mu.ui.data;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mu.ui.dialogs.*;

public class ButtonData implements ElementData{
    public Button build(){
        return new Button(Styles.defaultb);
    }

    public Table explorerSettings(UIExplorerDialog dialog){
        return new Table();
    }
}