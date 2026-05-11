package mu.ui.data;

import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mu.ui.dialogs.*;

public abstract class ElementData<T extends Element>{
    // All fields that can be set easily through Reflect
    public static Seq<String> fieldNames;
    // All fields that must be scaled through scl()
    public static Seq<String> sclFieldNames;
    
    public abstract T build();

    public abstract Table explorerSettings(UIExplorerDialog dialog);

    public void copyFields(T elem){
        Seq<String> fieldNames = (Seq<String>) Reflect.get(this.getClass(), "fieldNames");
        Seq<String> sclFieldNames = (Seq<String>) Reflect.get(this.getClass(), "sclFieldNames");
        // TODO: maybe i should do it through annotations

        for(String field : fieldNames){
            if(sclFieldNames.contains(field)){
                Reflect.set(elem, field, Scl.scl(Reflect.get(this, field)));
            }else{
                Reflect.set(elem, field, Reflect.get(this, field));
            }
        }
    }
}