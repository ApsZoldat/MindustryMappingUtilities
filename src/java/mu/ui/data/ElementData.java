package mu.ui.data;

import arc.scene.*;
import arc.scene.ui.layout.*;
import mu.ui.dialogs.*;

public interface ElementData<T extends Element>{
    public T build();

    public Table explorerSettings(UIExplorerDialog dialog);
}