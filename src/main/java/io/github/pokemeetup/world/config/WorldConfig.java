package io.github.pokemeetup.world.config;

public class WorldConfig {
    private long seed;
    private int chunkSize = 16;
    private int tileSize = 32;

    public WorldConfig(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getTileSize() {
        return tileSize;
    }
}
