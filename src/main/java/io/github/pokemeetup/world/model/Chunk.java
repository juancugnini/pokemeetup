package io.github.pokemeetup.world.model;

public class Chunk {
    private int chunkX;
    private int chunkY;
    private int[][] tileData;

    public Chunk(int chunkX, int chunkY, int[][] tileData) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.tileData = tileData;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    public int[][] getTileData() {
        return tileData;
    }
}
