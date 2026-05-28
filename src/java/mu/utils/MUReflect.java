package mu.utils;

import arc.scene.ui.layout.*;
import arc.util.*;
import mu.utils.MUAnnotations.*;

public class MUReflect{
    public static void copyFields(Object a, Object b){
        for(var field : a.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(NoCopy.class)) continue;

            String name = field.getName();
            if(field.isAnnotationPresent(RequireScl.class)){
                Reflect.set(b, name, Scl.scl(Reflect.get(a, name)));
            }else{
                Reflect.set(b, name, Reflect.get(a, name));
            }
        }
    }

    public static void copyChildFields(Object obj, Class<?> parentClass){
        for(var field : obj.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(NoCopy.class)) continue;

            String name = field.getName();
            if(field.isAnnotationPresent(RequireScl.class)){
                Reflect.set(parentClass, obj, name, Scl.scl(Reflect.get(obj, name)));
            }else{
                Reflect.set(parentClass, obj, name, Reflect.get(obj, name));
            }
        }
    }
}