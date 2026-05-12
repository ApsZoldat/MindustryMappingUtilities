package mu.utils;

import arc.files.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

public class SubtitleRandomizer{
    private final static Rand rand = new Rand();

    public LoadedMod mod;

    public Seq<String> subtitles = new Seq<>();

    public SubtitleRandomizer(LoadedMod mod){
        this.mod = mod;
    }

    public void removeMaxLength(){
        ModMeta newMeta = new ModMeta(){
            @Override
            public String shortDescription(){
                return subtitle;
            }
        };
        copyMeta(mod.meta, newMeta);
        Reflect.set(mod, "meta", newMeta);
    }

    public void copyMeta(ModMeta a, ModMeta b){
        for(var field : a.getClass().getDeclaredFields()){
            try{
                field.setAccessible(true);
                field.set(b, field.get(a));
            }catch (Exception e){
                Log.err("Mapping Utilities meta copying failed.", e);
            }
        }
    }

    public void fetchSubtitles(){
        subtitles.addAll(mod.root.child("subtitles").readString("UTF-8").split("\n"));
    }

    public void randomize(){
        mod.meta.subtitle = subtitles.get(rand.nextInt(subtitles.size));
    }
}