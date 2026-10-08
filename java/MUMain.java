package mu;

import arc.Events;
import arc.util.*;
import mindustry.game.EventType;
import mindustry.mod.*;

public class MUMain extends Mod{
    public MUMain(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Log.info("hii :3");
        });
    }
}
