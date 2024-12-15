package io.github.pokemeetup.world.service;

import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import io.github.pokemeetup.world.config.WorldConfig;

import java.util.Map;

public class WorldGenerator {
    private final WorldConfig config;
    private final Map<BiomeType, Biome> biomes;

    public WorldGenerator(WorldConfig config, Map<BiomeType, Biome> biomes) {
        this.config = config;
        this.biomes = biomes;
    }

    public int[][] generateChunk(int chunkX, int chunkY) {
        BiomeType selectedBiome = (chunkX + chunkY) % 2 == 0 ? BiomeType.PLAINS : BiomeType.DESERT;
        Biome biome = biomes.get(selectedBiome);

        int[][] tiles = new int[config.getChunkSize()][config.getChunkSize()];
        for (int x = 0; x < config.getChunkSize(); x++) {
            for (int y = 0; y < config.getChunkSize(); y++) {
                tiles[x][y] = pickTileFromDistribution(biome.getTileDistribution());
            }
        }

        return tiles;
    }

    private int pickTileFromDistribution(Map<Integer,Integer> distribution) {
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        int roll = MathUtils.random(total - 1);
        int cumulative = 0;
        for (Map.Entry<Integer,Integer> entry : distribution.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return distribution.keySet().iterator().next();
    }
}
