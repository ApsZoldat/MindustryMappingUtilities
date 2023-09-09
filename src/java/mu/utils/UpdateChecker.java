package mu.utils;

import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.mod.Mods.LoadedMod;
import mu.MapUtilitiesMod;

import static arc.Core.bundle;
import static mindustry.Vars.*;

public class UpdateChecker{
    public static void run(){
        LoadedMod mod = mods.getMod(MapUtilitiesMod.class);
        Http.get(ghApi + "/repos/" + mod.getRepo() + "/releases", res -> {
            var json = Jval.read(res.getResultAsString());
            Jval.JsonArray releases = json.asArray();

            if(releases.size == 0){
                Log.err("No Mapping Utilities releases available for auto-updating");
                return;
            }

            Jval release = releases.get(0);
            String modVersion = mod.meta.version;
            modVersion = (modVersion.contains(".") ? modVersion : modVersion + ".0");
            if(release.getString("tag_name").replace("v", "").equals(modVersion)){
                Log.info("Mapping Utilities running on latest version");
                return;
            }

            ui.showConfirm("@mu_new_version", bundle.format("mu_update_info", release.getString("tag_name")), () -> {
                String releaseUrl = release.getString("url");
                mod.file.delete();
                ui.mods.githubImportMod(mod.getRepo(), mod.isJava(), releaseUrl.substring(releaseUrl.lastIndexOf("/") + 1));
            });
        }, thr -> Log.err("Can't fetch Mapping Utilities releases for auto-updating", thr));
    }
}
