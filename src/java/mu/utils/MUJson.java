package mu.utils;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.ctype.*;
import mindustry.io.*;
import mu.editor.*;
import mu.editor.blocks.*;
import mu.editor.blocks.tools.*;
import mu.editor.blocks.brushes.*;
import java.io.*;

public class MUJson extends Json{
    public static MUJson jsonInstance;

    // Singletons always get modified instead of copied on deserialization
    public Seq<Object> singletonObjects = new Seq<>();
    // This object only gets modified too
    public Object baseObject;

    public static MUJson get(){
        if(jsonInstance == null){
            jsonInstance = new MUJson();
        }
        return jsonInstance;
    }

    public static void addSingleton(Object obj){
        get().singletonObjects.add(obj);
    }

    public static void writeBytes(Object value, Class<?> elementType, DataOutputStream output){
        MUJson json = get();
        json.setWriter(new UBJsonWriter(output));
        json.writeValue(value, value == null ? null : value.getClass(), elementType);
    }

    public static <T> T readBytes(Class<T> type, Class<?> elementType, DataInputStream input) throws IOException{
        MUJson json = get();
        return json.readValue(type, elementType, new UBJsonReader().parseWihoutClosing(input));
    }

    public static String write(Object object){
        MUJson json = get();
        return json.toJson(object, object.getClass());
    }

    public static <T> T copy(T object, T dest){
        MUJson json = get();
        json.copyFields(object, dest);
        return dest;
    }

    public static <T> T copy(T object){
        MUJson json = get();
        return read((Class<T>)object.getClass(), write(object));
    }

    public static <T> T read(Class<T> type, String string){
        MUJson json = get();
        return json.fromJson(type, string.replace("io.anuke.", ""));
    }

    public static <T> T read(Class<T> type, T base, String string){
        MUJson json = get();
        return json.fromBaseJson(type, base, string.replace("io.anuke.", ""));
    }

    public static String print(String in){
        MUJson json = get();
        return json.prettyPrint(in);
    }

    public static void classTag(String tag, Class<?> type){
        get().addClassTag(tag, type);
    }

    public static void classTags(ObjectMap<String, Class<?>> tags){
        for(var entry : tags.entries()){
            get().addClassTag(entry.key, entry.value);
        }
    }

    @Override
    public void writeValue(Object value, Class knownType, Class elementType){
        if(value instanceof MappableContent c){
            try{
                getWriter().value(c.name);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }else{
            super.writeValue(value, knownType, elementType);
        }
    }

    @Override
    protected String convertToString(Object object){
        if(object instanceof MappableContent c) return c.name;
        return super.convertToString(object);
    }

    @Override
    protected <T> Class<T> resolveClass(String className){
        Class<T> result = super.resolveClass(className);
        if(Serializable.class.isAssignableFrom(result) || JsonSerializable.class.isAssignableFrom(result)){
            return result;
        }
        throw new SerializationException("Class deserialization not allowed: " + result);
    }

    @Override
    public <T> T fromJson(Class<T> type, String json){
        return fromBaseJson(type, null, json);
    }

    public <T> T fromBaseJson(Class<T> type, T base, String json){
        this.baseObject = base;
        T object = readValue(type, null, new JsonReader().parse(json));
        this.baseObject = null;
        return object;
    }

    @Override
    protected Object newInstance(Class type){
        for(Object obj : singletonObjects){
            if(type == obj.getClass()) return obj;
        }

        if(baseObject == null || baseObject.getClass() != type){
            return super.newInstance(type);
        }
        return baseObject;
    }
}