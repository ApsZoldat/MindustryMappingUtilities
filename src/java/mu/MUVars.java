package mu;

import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mu.ui.*;

public class MUVars{
    // UI
    public static final BetterBannedContentDialog revealedBlocksDialog = new BetterBannedContentDialog("@rules.revealedblocks", ContentType.block, b -> true);
    static{
        revealedBlocksDialog.isRevealed = true;
    }
    public static BetterBannedContentDialog<Block> betterBannedBlocks = new BetterBannedContentDialog<>("@bannedblocks", ContentType.block, Block::canBeBuilt);
    public static BetterBannedContentDialog<UnitType> betterBannedUnits = new BetterBannedContentDialog<>("@bannedunits", ContentType.unit, u -> !u.isHidden());
    public static final PlanetBackgroundDialog planetBackgroundDialog = new PlanetBackgroundDialog();
}
