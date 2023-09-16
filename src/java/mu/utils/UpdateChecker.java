package mu.utils;

import arc.input.KeyCode;
import arc.util.Align;
import arc.util.Http;
import arc.util.Log;
import arc.util.Strings;
import arc.util.serialization.Jval;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.dialogs.BaseDialog;
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
            if(Strings.parseFloat(release.getString("tag_name").replace("v", "")) <= Strings.parseFloat(modVersion)){
                Log.info("Mapping Utilities running on latest version");
                return;
            }

            updateDialog(mod, release);
        }, thr -> Log.err("Can't fetch Mapping Utilities releases for auto-updating", thr));
    }

    private static void updateDialog(LoadedMod mod, Jval release){
        BaseDialog dialog = new BaseDialog("@mu_new_version");
        dialog.cont.pane(table -> {
            table.add(bundle.format("mu_update_info", release.getString("tag_name"))).width(mobile ? 400f : 500f).wrap().pad(4f).get().setAlignment(Align.center, Align.center);
            table.row();
            if(release.getString("body") != null){
                table.add("@mu_update_notes").color(Pal.accent).center().padTop(20f).row();
                table.add(release.getString("body")).wrap().width(mobile ? 400f : 500f).row();
            }
        });
        dialog.buttons.defaults().size(200f, 54f).pad(2f);
        dialog.buttons.button("@cancel", Icon.cancel, dialog::hide);
        dialog.buttons.button("@ok", Icon.ok, () -> {
            dialog.hide();
            String releaseUrl = release.getString("url");
            ui.mods.githubImportMod(mod.getRepo(), mod.isJava(), releaseUrl.substring(releaseUrl.lastIndexOf("/") + 1));
        });
        dialog.keyDown(KeyCode.enter, () -> {
            dialog.hide();
            String releaseUrl = release.getString("url");
            ui.mods.githubImportMod(mod.getRepo(), mod.isJava(), releaseUrl.substring(releaseUrl.lastIndexOf("/") + 1));
        });
        dialog.keyDown(KeyCode.escape, dialog::hide);
        dialog.keyDown(KeyCode.back, dialog::hide);
        dialog.show();
    }
}
