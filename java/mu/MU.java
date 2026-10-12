package mu;

import arc.Events;
import arc.struct.*;
import arc.util.*;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mu.modules.*;
import mu.ui.dialogs.*;

public class MU extends Mod{
    /** All registered modules; filled on {@link EventType.ClientLoadEvent}, then initialized once. */
    public static Seq<MUModule> modules = new Seq<>();

    /** Singleton instance of the settings dialog; created once at startup. */
    public static MUSettingsDialog settingsDialog;

    public MU(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            // Module registration goes here - dialog instances exist by the time this event fires
            modules.add(new RulesDialogModule());

            // The settings dialog reads MU.modules when it is shown, so it must be created after the lines above
            settingsDialog = new MUSettingsDialog();

            // The settings menu rebuilds and clears its private menu table on every open, so the button is re-added whenever it goes missing
            try{
                Table menu = Reflect.get(Vars.ui.settings, "menu");
                TextButton button = menu.button("@settings.mapping_utilities", Icon.editor, settingsDialog::show).marginLeft(8f).get();
                menu.row();
                menu.update(() -> {
                    if(!menu.getChildren().contains(button)){
                        menu.add(button).marginLeft(8f);
                        menu.row();
                    }
                });
            }catch(Exception t){
                Log.err("[MU] Failed to add Mapping Utilities button to the settings menu", t);
            }

            Log.info("[MU] Initializing Mapping Utilities modules...");

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
