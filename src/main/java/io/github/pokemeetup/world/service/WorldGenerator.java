package io.github.pokemeetup.world.service;

import io.github.pokemeetup.world.model.BiomeType;

public interface WorldGenerator {
    void setSeedAndBiomes(long seed, java.util.Map<BiomeType, io.github.pokemeetup.world.model.Biome> biomes);
    int[][] generateChunk(int chunkX, int chunkY);
    io.github.pokemeetup.world.model.Biome getBiomeForChunk(int chunkX, int chunkY);
}
