package mu.ui.data;

import arc.scene.*;
import arc.struct.*;
import mu.ui.dialogs.*;

public interface ElementData<T extends Element>{;
    public T build();

    public T buildPreview(UIExplorerDialog dialog);
}