package mu;

import mindustry.ctype.ContentType;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mu.ui.ContentSelectionDialog;
import mu.ui.PlanetBackgroundDialog;
import mu.legacy.ui.LegacyRulesSearchDialog;

public class MUVars{
    // UI
    public static final LegacyRulesSearchDialog searchDialog = new LegacyRulesSearchDialog();
    public static final ContentSelectionDialog<Block> bannedBlocksDialog = new ContentSelectionDialog<>("@bannedblocks", ContentType.block, Block::canBeBuilt);
    public static final ContentSelectionDialog<UnitType> bannedUnitsDialog = new ContentSelectionDialog<>("@bannedunits", ContentType.unit, u -> !u.isHidden());
    public static final ContentSelectionDialog<Block> revealedBlocksDialog = new ContentSelectionDialog<>("@rules.revealed_blocks", ContentType.block, u -> true);
    static { revealedBlocksDialog.isRevealedBlocks = true; }
    public static final PlanetBackgroundDialog planetBackgroundDialog = new PlanetBackgroundDialog();
}
