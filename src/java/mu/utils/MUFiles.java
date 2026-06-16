package mu.utils;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.util.*;
import mindustry.maps.*;

import static mindustry.Vars.*;

public class MUFiles{
    public static void moveMapToFolder(Map map){
        Fi oldFile = map.file;
        String mapName = oldFile.nameWithoutExtension().replace(".msav", "");  // idk sometimes it fails
        Fi folder = customMapDirectory.child(mapName);
        Fi newFile = folder.child(mapName + ".msav");
    
        if(!folder.exists()){
            if(!folder.mkdirs()){
                Log.err("MUFiles: Could not create directory for map: @", folder.path());
                return;
            };
        }

        // Map file already is in the folder
        if (oldFile.parent().equals(folder)) return; 

        try{
            oldFile.copyTo(newFile);
    
            // Verify the copy was successful
            if(newFile.exists() && newFile.length() == oldFile.length()){
                oldFile.delete();

                Reflect.set(map, "file", newFile);
                Log.info("MUFiles: Successfully moved map file to @", newFile.path());
            }else{
                Log.err("MUFiles: Invalid map file copy.");
                newFile.delete(); 
            }
        }catch(Exception ex){
            Log.err("MUFiles: Exception during move: @", ex);
        }
    }
}
