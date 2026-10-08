package mu.mods;

import static arc.Core.settings;

public abstract class MUMod{
    public String settingName;

    public abstract void enable();
    public abstract void disable();

    public void update(){
        if(settings.getBool(settingName, true)){
            enable();
        }else{
            disable();
        }
    }
}