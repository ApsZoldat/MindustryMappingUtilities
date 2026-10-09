package mu.modules;

import static arc.Core.*;

/**
 * An atomic, restart-to-apply feature unit: its setting key and default come from the subclass constructor via {@code super(name, def)}.
 */
public abstract class MUModule{
    /** The setting key that enables this module. */
    public final String name;
    /** The setting's default value. */
    public final boolean def;

    public MUModule(String name, boolean def){
        this.name = name;
        this.def = def;
    }

    /** @return the module's setting; consulted once, by the startup init loop. */
    public boolean enabled(){
        return settings.getBool(name, def);
    }

    /** Runs once at startup if the module is enabled; may throw - the framework disables the module for the session instead of crashing. */
    public abstract void init();
}
