package mu.ui.data;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mu.ui.data.annotations.*;
import mu.ui.dialogs.*;

public abstract class ElementData<T extends Element>{;
    public abstract T build();

    public abstract T buildPreview(UIExplorerDialog dialog);

    public abstract Table explorerSettings(UIExplorerDialog dialog);

    public void copyFields(T elem){
        for(var field : this.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(NoCopy.class)) continue;

            String name = field.getName();
            if(field.isAnnotationPresent(RequireScl.class)){
                Reflect.set(elem, name, Scl.scl(Reflect.get(this, name)));
            }else{
                Reflect.set(elem, name, Reflect.get(this, name));
            }
        }
    }
}