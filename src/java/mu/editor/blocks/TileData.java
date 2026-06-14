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

    public static byte unpackFloorData(int mergedData){
        return (byte)((mergedData & 0xFF0000) >> 16);
    }

    public static byte unpackOverlayData(int mergedData){
        return (byte)((mergedData & 0xFF00) >> 8);
    }

    public static byte unpackBlockData(int mergedData){
        return (byte)(mergedData & 0xFF);
    }
}