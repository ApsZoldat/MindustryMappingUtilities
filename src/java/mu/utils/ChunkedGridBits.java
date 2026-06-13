package arc.struct;

import arc.func.*;
import arc.struct.*;
import arc.math.geom.*;
import arc.util.*;

public class ChunkedGridBits{
    public int chunkSizeBits = 3;  // Each chunk's width/heigth is 2^(this value)
    public int chunkSize;
    public int chunkMask;

    public LongMap<GridBits> chunks;

    public ChunkedGridBits(int chunkSizeBits){
        this.chunkSizeBits = chunkSizeBits;
    }

    public ChunkedGridBits(){
        chunkSize = 1 << chunkSizeBits;
        chunkMask = chunkSize - 1;
        this.chunks = new LongMap<>();
    }

    public void set(int x, int y){
        set(x, y, true);
    }

    public void set(int x, int y, boolean b){
        int chunkX = x >> chunkSizeBits;
        int chunkY = y >> chunkSizeBits;
        int localX = x & chunkMask;
        int localY = y & chunkMask;
        
        GridBits chunk = getChunk(chunkX, chunkY, b);
        chunk.set(localX, localY, b);
        if(b == false) removeChunkIfEmpty(chunkX, chunkY);
    }

    public boolean get(int x, int y){
        int chunkX = x >> chunkSizeBits;
        int chunkY = y >> chunkSizeBits;
        int localX = x & chunkMask;
        int localY = y & chunkMask;
        
        GridBits chunk = getChunk(chunkX, chunkY);
        return chunk != null && chunk.get(localX, localY);
    }

    public boolean isEmpty(){
        return chunks.isEmpty();
    }

    public void clear(){
        chunks.clear();
    }

    public long chunkKey(int chunkX, int chunkY){
        return ((long)chunkX << 32) | chunkY;
    }

    public GridBits getChunk(int chunkX, int chunkY){
        return getChunk(chunkX, chunkY, false);
    }

    public GridBits getChunk(int chunkX, int chunkY, boolean createNew){
        long key = chunkKey(chunkX, chunkY);
        GridBits chunk = chunks.get(key);

        if(chunk == null && createNew){
            chunk = new GridBits(chunkSize, chunkSize);
            chunks.put(key, chunk);
        }

        return chunk;
    }

    private void removeChunkIfEmpty(int chunkX, int chunkY){
        long key = chunkKey(chunkX, chunkY);
        GridBits chunk = chunks.get(key);
        
        if(chunk != null && ((Bits)Reflect.get(chunk, "bits")).isEmpty()){
            chunks.remove(key);
        }
    }

    // Consumer for each point where bits are set to true
    // Chunks are unordered.
    public void each(Intc2 cons){
        for(LongMap.Entry<GridBits> entry : chunks.entries()){
            long key = entry.key;
            GridBits chunk = entry.value;

            int chunkX = (int)(key >> 32);
            int chunkY = (int)key;

            for(int localX = 0; localX < chunkSize; localX++){
                for(int localY = 0; localY < chunkSize; localY++){
                    if(!chunk.get(localX, localY)) continue;
                    int globalX = (chunkX << chunkSizeBits) | localX;
                    int globalY = (chunkY << chunkSizeBits) | localY;
                    cons.get(globalX, globalY);
                }
            }
        }
    }
}
