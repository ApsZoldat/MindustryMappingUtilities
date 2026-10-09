package mu;

import arc.Events;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.mod.*;
import mu.modules.*;

public class MU extends Mod{
    /** All registered modules; filled on {@link EventType.ClientLoadEvent}, then initialized once. */
    public static Seq<MUModule> modules = new Seq<>();

    public MU(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            //module registration goes here - dialog instances exist by the time this event fires
            //modules.add(new ExampleModule());

            //the settings category reads MU.modules synchronously, so it must be registered after the lines above
            SettingsDialogMod.enable();

            Log.info("[MU] Initializing Mapping Utilities modules");

            Seq<String> failed = new Seq<>();

            for(MUModule module : modules){
                if(!module.enabled()) continue;

                try{
                    module.init();
                }catch(Throwable t){
                    Log.err("[MU] Failed to initialize module '" + module.name + "'", t);
                    failed.add(
                        "[stat]" + module.name + "[] - [red]" +
                        (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()) + "[]"
                    );
                }
            }

            if(failed.any()){
                StringBuilder text = new StringBuilder(
                    "[red]Mapping Utilities Modules Error[]\n\n" +
                    "The following modules failed to initialize and have been disabled for this session:\n\n"
                );

                for(String fail : failed){
                    text.append(fail).append('\n');
                }

                text.append("\nSee the log for the full stack traces.");

                // Show after load settles, like the game's own startup notices
                Time.runTask(4f, () -> Vars.ui.showInfo(text.toString()));
            }
        });
    }
}
