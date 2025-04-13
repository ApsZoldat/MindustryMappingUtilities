package mu;

import mindustry.ctype.ContentType;
import mu.ui.RevealedBlocksDialog;
import mu.ui.PlanetBackgroundDialog;

public class MUVars{
    // UI
    public static final RevealedBlocksDialog revealedBlocksDialog = new RevealedBlocksDialog("@rules.revealedblocks", ContentType.block, b -> true);
    public static final PlanetBackgroundDialog planetBackgroundDialog = new PlanetBackgroundDialog();
}
