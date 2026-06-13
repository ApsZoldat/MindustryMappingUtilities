package mu.editor.blocks;

import mindustry.world.*;

import static mindustry.Vars.content;

public enum TileData{
    floor,
    overlay,
    block,
    team,
    rotation,
    data,
    extraData;

    public static int packMergedData(int blockData, int overlayData, int floorData){
        return blockData | (overlayData << 8) | (floorData << 16);
    }

    public static int unpackBlockData(int mergedData){
        return mergedData & 0xFF;
    }

    public static int unpackOverlayData(int mergedData){
        return mergedData & 0xFF00;
    }

    public static int unpackFloorData(int mergedData){
        return mergedData & 0xFF0000;
    }
}