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
            apply(jsonInstance);
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

    public static void apply(Json json){
        Reflect.invoke(JsonIO.class, "apply", new Object[]{json}, Json.class);

        // GridBits to Base64 serializer
        // TODO: actually rethink whether this is the best format
        /*
        json.setSerializer(GridBits.class, new Serializer<GridBits>(){
            @Override
            public void write(Json json, GridBits object, Class knownType){
                json.writeObjectStart();

                json.writeValue("width", object.width());
                json.writeValue("height", object.height());

                long[] longs = Reflect.get(Bits.class, Reflect.get(object, "bits"), "bits");
                byte[] bytes = new byte[longs.length * Long.BYTES];

                // long[] to byte[] conversion
                for(int i = 0; i < longs.length; i++){
                    for(int j = 0; j < Long.BYTES; j++){
                        bytes[i * Long.BYTES + j] = (byte) ((longs[i] >> (8 * j)) & 0xFF);
                    }
                }

                String encoded = new String(Base64Coder.encode(bytes));
                json.writeValue("bits", encoded);

                json.writeObjectEnd();
            }

            @Override
            public GridBits read(Json json, JsonValue jsonData, Class type){
                int width = jsonData.getInt("width");
                int height = jsonData.getInt("height");

                String encoded = jsonData.getString("bits");
                byte[] bytes = Base64Coder.decode(encoded);
                GridBits grid = new GridBits(width, height);

                long[] longs = Reflect.get(Bits.class, Reflect.get(grid, "bits"), "bits");

                // byte[] to long[] conversion
                for(int i = 0; i < longs.length; i++){
                    for(int j = 0; j < Long.BYTES; j++){
                        longs[i] |= (bytes[i * Long.BYTES + j] & 0xFFL) << (8 * j);
                    }
                }

                return grid;
            }
        });*/
    }

    /*public static String printBytes(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < bytes.length; i++){
            // Convert byte to 8-bit binary string
            String binaryString = String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0');
            result.append(binaryString);

            // Add separator between bytes (except after the last one)
            if(i < bytes.length - 1){
                result.append(" | ");
            }
        }
        return result.toString();
    }*/

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